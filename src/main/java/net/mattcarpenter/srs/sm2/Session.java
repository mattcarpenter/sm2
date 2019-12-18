package net.mattcarpenter.srs.sm2;

import java.util.*;

public class Session {
    private Map<Item, SessionItemStatistics> itemStatisticsMap = new HashMap<>();

    public void applyReview(Review review) {
        Item item = review.getItem();
        SessionItemStatistics itemStatistics = itemStatisticsMap.computeIfAbsent(item, k -> new SessionItemStatistics());
        itemStatistics.setMostRecentScore(review.getScore());

        if (review.getScore() < 3) {
            itemStatistics.setLapsedDuringSession(true);
        }
    }

    public Map<Item, SessionItemStatistics> getItemStatistics() {
        return this.itemStatisticsMap;
    }
}
