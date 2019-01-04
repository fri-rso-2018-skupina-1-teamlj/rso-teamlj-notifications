package si.fri.rso.teamlj.notifications.entities;

import lombok.Getter;
import lombok.Setter;
import si.fri.rso.teamlj.notifications.dtos.MapEntity;
import si.fri.rso.teamlj.notifications.dtos.Payment;
import si.fri.rso.teamlj.notifications.dtos.User;

import javax.persistence.Transient;
import java.util.List;

//@Entity(name = "notificationTable")
//@NamedQueries(value =
//        {
//                @NamedQuery(name = "Notification.getAll", query = "SELECT n FROM notificationTable n")
//        })
public class Notification {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Getter
//    @Setter
//    private Integer id;

    public Notification() {

    }

    public Notification(float currentLocationLatitude, float currentLocationLongitude, String nearbyLocationName, float nearbyLocationLatitude, float nearbyLocationLongitude, int remainingUserSubscriptionDays, User user) {
        this.currentLocationLatitude = currentLocationLatitude;
        this.currentLocationLongitude = currentLocationLongitude;
        this.nearbyLocationName = nearbyLocationName;
        this.nearbyLocationLatitude = nearbyLocationLatitude;
        this.nearbyLocationLongitude = nearbyLocationLongitude;
        this.remainingUserSubscriptionDays = remainingUserSubscriptionDays;
        this.user = user;
    }

    @Getter
    @Setter
    private float currentLocationLatitude;

    @Getter
    @Setter
    private float currentLocationLongitude;

    @Getter
    @Setter
    private String nearbyLocationName;

    @Getter
    @Setter
    private float nearbyLocationLatitude;

    @Getter
    @Setter
    private float nearbyLocationLongitude;

    @Getter
    @Setter
    private int remainingUserSubscriptionDays;

    @Transient
    @Getter
    @Setter
    private List<MapEntity> mapEntityList;

    @Transient
    @Getter
    @Setter
    private User user;

    @Transient
    @Getter
    @Setter
    private Payment payment;

}
