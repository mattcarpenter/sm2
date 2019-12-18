package net.mattcarpenter.srs.sm2;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Set;

public class SchedulerTest {

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

        // update the item interval given a session that resulted in a '5' score review and no lapses
        SessionItemStatistics statistics = new SessionItemStatistics(false, 5);
        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 1, "item interval should be 1 day");

        // update the interval given another session that resulted in a '5' score review and no lapses
        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 6, "item interval should jump from 1 day to 6 days");

        // update the interval given another session that resulted in a '5' score review and no lapses
        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 17);

        // update the interval given another session that resulted in a '5' score review and no lapses
        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 49);
    }

    @Test
    public void updateItemInterval_customConsecutiveCorrectIntervalMapping() {
        Scheduler scheduler = Scheduler.builder().build();
        Item item = Item.builder().interval(0).build();

        // update the item interval given a session that resulted in a '5' score review and no lapses
        SessionItemStatistics statistics = new SessionItemStatistics(false, 5);
        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 1, "item interval should be 1 day");

        // update the interval given another session that resulted in a '5' score review and no lapses
        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 6, "item interval should jump from 1 day to 6 days");

        // update the interval given another session that resulted in a '5' score review and no lapses
        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 17);

        // update the interval given another session that resulted in a '5' score review and no lapses
        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 49);
    }

    @Test
    public void updateItemInterval_resets_lapseFollowedBySuccess() {
        Map<Integer, Integer> intervalMapping = Map.ofEntries(
                Map.entry(1, 1),
                Map.entry(2, 2),
                Map.entry(3, 4)
        );

        Scheduler scheduler = Scheduler.builder()
                .consecutiveCorrectIntervalMappings(intervalMapping)
                .build();

        Item item = Item.builder().interval(0).build();

        // update the item interval given a session that resulted in a '5' score review and no lapses
        SessionItemStatistics statistics = new SessionItemStatistics(false, 5);
        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 1, "item interval should be 1 day");

        // update the item interval given a session that resulted in a '5' score review and no lapses
        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 2, "item interval should be 2 days");

        // update the item interval given a session that resulted in a '5' score review and no lapses
        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 4, "item interval should be 4 days");

        // update the item interval given a session that resulted in a '5' score review and no lapses
        scheduler.updateItemInterval(item, statistics);
        Assert.assertEquals(item.getInterval(), 12, "item interval should be 12 days");
    }
}
