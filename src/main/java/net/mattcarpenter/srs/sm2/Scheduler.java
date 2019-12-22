package net.mattcarpenter.srs.sm2;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

import java.util.*;

@Builder
public class Scheduler {

    private final float MIN_EASINESS_FACTOR = 1.3f;
    private final int HOURS_PER_DAY = 24;

    private final Map<Integer, Float> defaultConsecutiveCorrectIntervalMappings = Map.ofEntries(
            Map.entry(1, 1f),
            Map.entry(2, 6f)
    );

    @Getter
    @Setter
    @Builder.Default
    private Map<Integer, Float> consecutiveCorrectIntervalMappings = new HashMap<>();

    @Builder.Default
    private Set<Item> items = new HashSet<>();

    @Builder.Default
    private TimeProvider timeProvider = new JodaTimeProvider();

    public void addItem(Item item) {
        items.add(item);
    }

    public Set<Item> getItems() {
        return items;
    }

    public void applySession(Session session) {
        session.getItemStatistics().forEach((item, statistics) -> {
            updateItemInterval(item, statistics);
            updateItemSchedule(item);
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
            Float fixedInterval = getConsecutiveCorrectInterval(item.getConsecutiveCorrectCount());
            item.setInterval(Optional.ofNullable(fixedInterval)
                            .orElse((float)Math.round(item.getInterval() * item.getEasinessFactor())));
        }
    }

    protected void updateItemSchedule(Item item) {
        int intervalDaysWhole = (int)item.getInterval();
        float intervalDaysFraction = item.getInterval() - intervalDaysWhole;

        DateTime dueDate = timeProvider.getNow()
                .plusDays(intervalDaysWhole)
                .plusHours(Math.round(HOURS_PER_DAY * intervalDaysFraction));

        item.setDueDate(dueDate);
    }

    protected Float getConsecutiveCorrectInterval(int consecutiveCorrect) {
        return consecutiveCorrectIntervalMappings.getOrDefault(consecutiveCorrect,
                defaultConsecutiveCorrectIntervalMappings.get(consecutiveCorrect));
    }
}
