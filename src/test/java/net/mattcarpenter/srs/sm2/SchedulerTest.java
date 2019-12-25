package net.mattcarpenter.srs.sm2;

import com.google.common.collect.ImmutableMap;
import net.mattcarpenter.srs.sm2.utils.MockTimeProvider;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Set;

public class SchedulerTest {

    private MockTimeProvider mockTimeProvider;
    private DateTime initialDate;

    @BeforeClass
    public void before() {
        String pattern = "yyyy-mm-dd hh:mm:ss aa";
        initialDate = DateTime.parse("2019-01-01 12:00:00 AM", DateTimeFormat.forPattern(pattern));
        mockTimeProvider = new MockTimeProvider(initialDate);
    }

    @Test
    public void addItem_dedupes() {
        Item item = Item.builder().build();
        Item item2 = Item.builder().build();

        Scheduler scheduler = Scheduler.builder().build();
        scheduler.addItem(item);
        scheduler.addItem(item);
        scheduler.addItem(item2);

        Set<Item> items = scheduler.getItems();
        Assert.assertEquals(items.size(), 2);
    }

    @Test
    public void updateItemInterval_allCorrect_SM2_defaults() {
        Scheduler scheduler = Scheduler.builder().build();
        Item item = Item.builder().interval(0).build();

        SessionItemStatistics statistics = new SessionItemStatistics(false, 5);
        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 1, "item interval should be 1 day");

        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 6, "item interval should jump from 1 day to 6 days");

        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 17);

        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 49);
    }

    @Test
    public void updateItemInterval_resetsAfterLapse() {
        Scheduler scheduler = Scheduler.builder().build();
        Item item = Item.builder().interval(0).build();

        SessionItemStatistics statistics = new SessionItemStatistics(false, 5);
        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 1, "item interval should be 1 day");

        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 6, "item interval should jump from 1 day to 6 days");

        // represents a session where a lapse occurred but item answered successfully at the end
        SessionItemStatistics statistics2 = new SessionItemStatistics(true, 5);
        scheduler.updateItemInterval(item, statistics2);
        Assert.assertEquals(item.getInterval(), 1);

        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 6);
    }

    @Test
    public void updateItemInterval_failedSession() {
        Scheduler scheduler = Scheduler.builder().build();
        Item item = Item.builder().interval(0).build();

        SessionItemStatistics statistics = new SessionItemStatistics(true, 2);
        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 0);
    }

    @Test
    public void updateItemInterval_customConsecutiveCorrectIntervalMapping() {
        Map<Integer, Float> intervalMapping = ImmutableMap.of(
                1, 1f,
                2, 2f,
                3, 4f
        );

        Scheduler scheduler = Scheduler.builder()
                .consecutiveCorrectIntervalMappings(intervalMapping)
                .build();

        Item item = Item.builder().interval(0).build();

        SessionItemStatistics statistics = new SessionItemStatistics(false, 5);
        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 1, "item interval should be 1 day");

        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 2, "item interval should be 2 days");

        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 4, "item interval should be 4 days");

        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 12, "item interval should be 12 days");
    }

    @Test
    public void getConsecutiveCorrectInterval_getsDefaults() {
        Scheduler scheduler = Scheduler.builder().build();
        Assert.assertEquals(scheduler.getConsecutiveCorrectInterval(1).floatValue(), 1f);
        Assert.assertEquals(scheduler.getConsecutiveCorrectInterval(2).floatValue(), 6f);
        Assert.assertNull(scheduler.getConsecutiveCorrectInterval(3));
    }

    @Test
    public void getConsecutiveCorrectInterval_getsBothCustomAndDefault() {
        Map<Integer, Float> intervalMapping = ImmutableMap.of(
                1, 1f,
                2, 4f
        );

        Scheduler scheduler = Scheduler.builder()
                .consecutiveCorrectIntervalMappings(intervalMapping)
                .build();

        Assert.assertEquals(scheduler.getConsecutiveCorrectInterval(1).floatValue(), 1f);
        Assert.assertEquals(scheduler.getConsecutiveCorrectInterval(2).floatValue(), 4f);
        Assert.assertNull(scheduler.getConsecutiveCorrectInterval(3));
    }

    @Test
    public void updateItemSchedule_computesDueDate_wholeDayInterval() {
        Item item = Item.builder()
                .interval(1f)
                .build();

        Scheduler scheduler = Scheduler.builder()
                .timeProvider(mockTimeProvider)
                .build();

        scheduler.updateItemSchedule(item);

        // interval of 1 should return a due date that is one day ahead of the initial
        Assert.assertEquals(item.getDueDate(), mockTimeProvider.getNow().plusDays(1));
    }

    @Test
    public void updateItemSchedule_computesDueDate_partialDayInterval() {
        Item item = Item.builder()
                .interval(1.5f)
                .build();

        Scheduler scheduler = Scheduler.builder()
                .timeProvider(mockTimeProvider)
                .build();

        scheduler.updateItemSchedule(item);

        // interval of 1.5 should return a due date that is one 1 day 12 hours ahead of now
        Assert.assertEquals(item.getDueDate(), initialDate.plusDays(1).plusHours(12));
    }

    @Test
    public void applySession_schedules_ok() {
        Item item = Item.builder().build();
        Session session = new Session();
        Scheduler scheduler = Scheduler.builder().timeProvider(mockTimeProvider).build();

        Review review = new Review(item, 5);
        session.applyReview(review);

        // apply first session and check that the due date is 1 days from the initial date
        scheduler.applySession(session);
        Assert.assertEquals(item.getDueDate(), initialDate.plusDays(1));
        Assert.assertEquals(item.getConsecutiveCorrectCount(), 1);

        // apply the first session again and check that the due date is 7 days from the initial date
        scheduler.applySession(session);
        Assert.assertEquals(item.getDueDate(), initialDate.plusDays(6));
        Assert.assertEquals(item.getConsecutiveCorrectCount(), 2);
    }

    @Test
    public void applySession_schedules_lapsed() {
        Item item = Item.builder().build();
        Session session = new Session();
        Scheduler scheduler = Scheduler.builder().timeProvider(mockTimeProvider).build();

        Review review = new Review(item, 5);
        session.applyReview(review);

        // apply first session and check that the due date is 1 days from the initial date
        scheduler.applySession(session);
        Assert.assertEquals(item.getDueDate(), initialDate.plusDays(1));
        Assert.assertEquals(item.getConsecutiveCorrectCount(), 1);

        // apply the first session again and check that the due date is 7 days from the initial date
        Session session2 = new Session();
        Review review2 = new Review(item, 0);
        Review review3 = new Review(item, 5);
        session2.applyReview(review2);
        session2.applyReview(review3);
        scheduler.applySession(session2);
        Assert.assertEquals(item.getDueDate(), initialDate.plusDays(1));
        Assert.assertEquals(item.getConsecutiveCorrectCount(), 1);
    }

    @Test
    public void consecutiveCorrectIntervalMappings_get_set() {
        Scheduler scheduler = Scheduler.builder().build();
        Map<Integer, Float> intervalMapping = ImmutableMap.of(
                1, 1f,
                2, 2f,
                3, 4f
        );

        scheduler.setConsecutiveCorrectIntervalMappings(intervalMapping);

        Assert.assertEquals(scheduler.getConsecutiveCorrectIntervalMappings(), intervalMapping);
    }
}
