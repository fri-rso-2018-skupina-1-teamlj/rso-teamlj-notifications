package si.fri.rso.teamlj.notifications.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

public class User {

    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    private String firstName;

    @Getter
    @Setter
    private String lastName;

    @Getter
    @Setter
    private String address;

    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    private boolean inUse;

    @Getter
    @Setter
    private Instant dateOfBirth;

}
