package net.mattcarpenter.srs.sm2;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Builder
public class Scheduler {

    private final float MIN_EASINESS_FACTOR = 1.3f;
    private final Map<Integer, Integer> defaultConsecutiveCorrectIntervalMappings = Map.ofEntries(
            Map.entry(1, 1),
            Map.entry(2, 6)
    );

    @Getter
    @Setter
    @Builder.Default
    private Map<Integer, Integer> consecutiveCorrectIntervalMappings = new HashMap<>();

    @Builder.Default
    private Set<Item> items = new HashSet<>();

    public void addItem(Item item) {
        items.add(item);
    }

    public Set<Item> getItems() {
        return items;
    }

    public void applySession(Session session) {
        session.getItemStatistics().forEach((item, statistics) -> {
            updateItemInterval(item, statistics);
            // todo: update due date given new intervals
        });
    }

    protected void updateItemInterval(Item item, SessionItemStatistics statistics) {
        if (statistics.isLapsedDuringSession() && statistics.getMostRecentScore() > 2) {

            // item lapsed but the most recent review was successful.
            // reset interval and correct count without updating the item's easiness factor
            item.setConsecutiveCorrectCount(1);
            item.setInterval(getConsecutiveCorrectInterval(1));
        } else if (statistics.getMostRecentScore() < 3 ) {

            // last review for this item was not successful. set interval and consecutive correct count to 0
            item.setInterval(0);
            item.setConsecutiveCorrectCount(0);
        } else {
            // item was recalled successfully during this session without a lapse; increment the correct count
            item.setConsecutiveCorrectCount(item.getConsecutiveCorrectCount() + 1);

            // review was successful. update item easiness factor then calculate new interval
            float newEasinessFactor = Math.max(MIN_EASINESS_FACTOR, (float)(item.getEasinessFactor()
                    + (0.1 - (5 - statistics.getMostRecentScore()) * (0.08 + (5 - statistics.getMostRecentScore()) * 0.02))));
            item.setEasinessFactor(newEasinessFactor);

            // either update interval based on a static mapping, or based on the previous interval * EF.
            // default static mappings are based on SM2 defaults (1 day then 6 days) but this can be overridden.
            Integer fixedInterval = getConsecutiveCorrectInterval(item.getConsecutiveCorrectCount());
            item.setInterval(Optional.ofNullable(fixedInterval)
                            .orElse(Math.round(item.getInterval() * item.getEasinessFactor())));
        }
    }

    protected Integer getConsecutiveCorrectInterval(int consecutiveCorrect) {
        return consecutiveCorrectIntervalMappings.getOrDefault(consecutiveCorrect,
                defaultConsecutiveCorrectIntervalMappings.get(consecutiveCorrect));
    }
}
