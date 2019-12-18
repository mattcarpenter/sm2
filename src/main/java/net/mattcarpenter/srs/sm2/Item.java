package net.mattcarpenter.srs.sm2;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Builder
@Getter
@Setter
public class Item {
    private int consecutiveCorrectCount;
    private Date lastReviewedDate;
    private Date dueDate;
    private int interval;

    @Builder.Default
    private float easinessFactor = 2.5f;

    @Builder.Default
    private String id = UUID.randomUUID().toString();
}
