package si.fri.rso.teamlj.notifications.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

public class Payment {

    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    private Instant dateOfPayment;

    @Getter
    @Setter
    private Instant endOfSubscription;

    @Getter
    @Setter
    private boolean subscription;

    @Getter
    @Setter
    private Integer userId;

}
