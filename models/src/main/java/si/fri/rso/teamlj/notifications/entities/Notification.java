package si.fri.rso.teamlj.notifications.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "notificationTable")
@NamedQueries(value =
        {
                @NamedQuery(name = "Notification.getAll", query = "SELECT n FROM notificationTable n")
        })
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Integer id;

    @Column(name = "todo")
    @Getter
    @Setter
    private String todo;


}
