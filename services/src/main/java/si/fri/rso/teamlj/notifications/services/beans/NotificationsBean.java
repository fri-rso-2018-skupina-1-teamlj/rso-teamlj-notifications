package si.fri.rso.teamlj.notifications.services.beans;

import com.kumuluz.ee.discovery.annotations.DiscoverService;
import si.fri.rso.teamlj.notifications.dtos.MapEntity;
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
import javax.ws.rs.core.GenericType;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

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
    private EntityManager em;

    @Inject
    private AppProperties appProperties;

    @PostConstruct
    private void init() {
        httpClient = ClientBuilder.newClient();
        //baseUrl = "http://localhost:8085"; // notifications
    }


    public Notification getNotification(Integer userId, Float latitude, Float longitude) {

        User user = getUser(userId);
        if (user == null) {
            log.warning("user does not exist/user was deleted");
            throw new NotFoundException();
        }

        // TODO - preveri a ma plačan subscription
        // TODO
        int remainingUserSubscriptionDays = 1;


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



        Notification notification = new Notification(nearestRentPointName, nearestRentPointLatitude,
                                                     nearestRentPointLongitude,
                                                     remainingUserSubscriptionDays, user);


        return notification;
    }

    public int getNearestRentPoint(float latitude, float longitude, float[][] locations) {
        // BASED ON: https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
        final int R = 6371; // Radius of the earth
        float lat1 = latitude;
        float lon1 = longitude;

        float tmpMin = 0;
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
//                    .target(baseUrlUsers.get()  + "/v1/users?where=userId:EQ:" + userId)
                    .target("http://localhost:8080/v1/users?where=userId:EQ:" + userId)
                    .request().get(new GenericType<User>() {
                    });
        } catch (WebApplicationException | ProcessingException e) {
            log.severe(e.getMessage());
            throw new InternalServerErrorException(e);
        }

    }

    public List<MapEntity> getMapEntities() {

        try {
            return httpClient
//                    .target(baseUrlUsers.get()  + "/v1/map")
                    .target("http://localhost:8084/v1/map")
                    .request().get(new GenericType<List<MapEntity>>() {
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
