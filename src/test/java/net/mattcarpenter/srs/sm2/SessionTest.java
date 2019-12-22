package net.mattcarpenter.srs.sm2;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SessionTest {

    @Test
    public void applyReview_updatesStatistics_ok() {
        Session session = new Session();
        Item item = Item.builder().build();
        Review review1 = new Review(item, 5);
        Review review2 = new Review(item, 5);
        session.applyReview(review1);
        session.applyReview(review2);

        SessionItemStatistics stats = session.getItemStatistics().get(item);
        Assert.assertFalse(stats.isLapsedDuringSession());
        Assert.assertEquals(stats.getMostRecentScore(), 5);
    }

    @Test
    public void applyReview_updatesStatistics_lapseThenSuccess() {
        Session session = new Session();
        Item item = Item.builder().build();
        Review review1 = new Review(item, 2);
        Review review2 = new Review(item, 4);
        session.applyReview(review1);
        session.applyReview(review2);

        SessionItemStatistics stats = session.getItemStatistics().get(item);
        Assert.assertTrue(stats.isLapsedDuringSession());
        Assert.assertEquals(stats.getMostRecentScore(), 4);
    }
}
