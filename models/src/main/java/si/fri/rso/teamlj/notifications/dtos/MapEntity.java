package si.fri.rso.teamlj.notifications.dtos;

import lombok.Getter;
import lombok.Setter;

public class MapEntity {

    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    private float latitude;

    @Getter
    @Setter
    private float longitude;

    @Getter
    @Setter
    private String locationString;

    @Getter
    @Setter
    private String locationName;

    @Getter
    @Setter
    private int numberOfAvailableBikes;

}
