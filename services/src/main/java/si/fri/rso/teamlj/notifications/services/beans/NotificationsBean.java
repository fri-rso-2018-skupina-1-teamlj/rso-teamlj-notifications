package si.fri.rso.teamlj.notifications.services.beans;

import com.kumuluz.ee.discovery.annotations.DiscoverService;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import si.fri.rso.teamlj.notifications.dtos.MapEntity;
import si.fri.rso.teamlj.notifications.dtos.Payment;
import si.fri.rso.teamlj.notifications.dtos.User;
import si.fri.rso.teamlj.notifications.entities.Notification;
import si.fri.rso.teamlj.notifications.services.configuration.AppProperties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static java.lang.Math.toIntExact;
import static java.time.temporal.ChronoUnit.DAYS;

@RequestScoped
public class NotificationsBean {

    private Logger log = Logger.getLogger(NotificationsBean.class.getName());

    private Client httpClient;

    @Inject
    @DiscoverService("rso-users")
    private Optional<String> baseUrlUsers;

    @Inject
    @DiscoverService("rso-map")
    private Optional<String> baseUrlMap;

    @Inject
    @DiscoverService("rso-payments")
    private Optional<String> baseUrlPay;

    @Inject
    @DiscoverService("rso-bikes")
    private Optional<String> baseUrlBike;

    @Inject
    private EntityManager em;

    @PostConstruct
    private void init() {
        httpClient = ClientBuilder.newClient();
        //baseUrl = "http://localhost:8085"; // notifications
    }

    @Timed(name = "get_notification_timed")
    @Counted(name = "get_notification_counter")
    @CircuitBreaker(requestVolumeThreshold = 3)
    @Timeout(value = 10, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "getNotificationFallback")
    public Notification getNotification(Integer userId, Float latitude, Float longitude) {

        User user = getUser(userId);
        if (user == null) {
            log.warning("user does not exist/user was deleted");
            throw new NotFoundException();
        }

        String resultPayment = getSubscriptionInfo(userId);
        if (resultPayment == null) {
            log.warning("payment does not exist");
            throw new NotFoundException();
        }

        int remainingUserSubscriptionDaysInt;
        if (!resultPayment.equals("Ni veljavne naročnine")) {
            Instant timeNow = Instant.now();
            Instant endOfSubscription = Instant.parse(resultPayment);

            Long remainingUserSubscriptionDays = DAYS.between(timeNow, endOfSubscription);
            remainingUserSubscriptionDaysInt = toIntExact(remainingUserSubscriptionDays);
        }
        else {
            remainingUserSubscriptionDaysInt = 0;
        }


        List<MapEntity> mapEntityList = getMapEntities();
        if (mapEntityList == null || mapEntityList.size() == 0) {
            log.warning("bike rents locations does not exist/bike rents locations were deleted");
            throw new NotFoundException();
        }

        float[][] locations = new float[mapEntityList.size()][2];
        String[] locationsName = new String[mapEntityList.size()];
        for (int i = 0; i < mapEntityList.size(); i++) {
            MapEntity mapEntity = mapEntityList.get(i);
            float latitudeMapEntity = mapEntity.getLatitude();
            float longitudeMapEntity = mapEntity.getLongitude();
            locations[i][0] = latitudeMapEntity;
            locations[i][1] = longitudeMapEntity;
            locationsName[i] = mapEntity.getLocationName();
        }

        int nearestRentPoint = getNearestRentPoint(latitude, longitude, locations);

        float nearestRentPointLatitude = locations[nearestRentPoint][0];
        float nearestRentPointLongitude = locations[nearestRentPoint][1];
        String nearestRentPointName = locationsName[nearestRentPoint];

        List<Integer> bikeIds = getBikeIds(nearestRentPointLatitude, nearestRentPointLongitude);

        Notification notification = new Notification(latitude, longitude,
                                                     nearestRentPointName,
                                                     nearestRentPointLatitude,
                                                     nearestRentPointLongitude,
                                                     remainingUserSubscriptionDaysInt,
                                                     user, bikeIds);

        return notification;
    }

    public Notification getNotificationFallback(Integer userId, Float latitude, Float longitude) {

        log.warning("getNotificationFallback method called");
        return new Notification();
    }

    public int getNearestRentPoint(float latitude, float longitude, float[][] locations) {
        // BASED ON: https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
        final int R = 6371; // Radius of the earth
        float lat1 = latitude;
        float lon1 = longitude;

        float tmpMin = Float.MAX_VALUE;
        int index = 0;

        for (int i = 0; i < locations.length; i++) {
            float lat2 = locations[i][0];
            float lon2 = locations[i][1];

            float latDistance = (float) Math.toRadians(lat2 - lat1);
            float lonDistance = (float) Math.toRadians(lon2 - lon1);
            float a = (float) Math.sin(latDistance / 2) * (float) Math.sin(latDistance / 2)
                    + (float) Math.cos(Math.toRadians(lat1)) * (float) Math.cos(Math.toRadians(lat2))
                    * (float) Math.sin(lonDistance / 2) * (float) Math.sin(lonDistance / 2);
            float c = 2 * (float) Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            float distance = R * c * 1000; // convert to meters

            distance = (float) Math.pow(distance, 2);
            float tmp = (float) Math.sqrt(distance);

            if (tmp < tmpMin) {
                tmpMin = tmp;
                index = i;
            }
        }

        return index;
    }

    public User getUser(Integer userId) {

        try {
            return httpClient
                    .target(baseUrlUsers.get()  + "/v1/users/" + userId)
//                    .target("http://localhost:8080/v1/users/" + userId)
                    .request().get(new GenericType<User>() {
                    });
        } catch (WebApplicationException | ProcessingException e) {
            log.severe(e.getMessage());
            throw new InternalServerErrorException(e);
        }

    }

    public String getSubscriptionInfo(Integer userId) {

        try {
            Response response =  httpClient
                    .target(baseUrlPay.get()  + "/v1/payments/subscribed/" + userId)
//                    .target("http://localhost:8083/v1/payments/subscribed/" + userId)
                    .request()
                    .build("PUT", Entity.json(""))
                    .invoke();


            return response.readEntity(String.class);
        } catch (WebApplicationException | ProcessingException e) {
            log.severe(e.getMessage());
            throw new InternalServerErrorException(e);
        }

    }

    public List<MapEntity> getMapEntities() {

        try {
            return httpClient
                    .target(baseUrlMap.get()  + "/v1/map")
//                    .target("http://localhost:8084/v1/map")
                    .request().get(new GenericType<List<MapEntity>>() {
                    });
        } catch (WebApplicationException | ProcessingException e) {
            log.severe(e.getMessage());
            throw new InternalServerErrorException(e);
        }

    }

    public List<Integer> getBikeIds(float latitude, float longitude) {

        try {
            return httpClient
                    .target(baseUrlBike.get()  + "/v1/bikes/" + latitude + "&" + longitude)
                    .request().get(new GenericType<List<Integer>>() {
                    });
        } catch (WebApplicationException | ProcessingException e) {
            log.severe(e.getMessage());
            throw new InternalServerErrorException(e);
        }

    }

    private void beginTx() {
        if (!em.getTransaction().isActive())
            em.getTransaction().begin();
    }

    private void commitTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().commit();
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().rollback();
    }
}
