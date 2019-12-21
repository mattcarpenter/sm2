package net.mattcarpenter.srs.sm2;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

import java.util.UUID;

@Builder
@Getter
@Setter
public class Item {
    private int consecutiveCorrectCount;
    private DateTime lastReviewedDate;
    private DateTime dueDate;
    private float interval;

    @Builder.Default
    private float easinessFactor = 2.5f;

    @Builder.Default
    private String id = UUID.randomUUID().toString();
}
