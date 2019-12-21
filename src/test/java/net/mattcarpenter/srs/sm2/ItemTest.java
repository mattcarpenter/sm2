package net.mattcarpenter.srs.sm2;

import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;

public class ItemTest {

    @Test
    public void builder_generatesUniqueId() {
        Item item1 = Item.builder().easinessFactor(1.1f).build();
        Item item2 = Item.builder().easinessFactor(1.2f).build();

        Assert.assertNotEquals(item1.getId(), item2.getId());
        Assert.assertEquals(item1.getEasinessFactor(), 1.1f);
        Assert.assertEquals(item2.getEasinessFactor(), 1.2f);
    }

    @Test
    public void builder_buildsItem() {
        final DateTime now = DateTime.now();
        Item item = Item.builder()
                .consecutiveCorrectCount(1)
                .easinessFactor(2.1f)
                .id("test-id")
                .dueDate(now)
                .lastReviewedDate(now)
                .build();

        Assert.assertEquals(item.getConsecutiveCorrectCount(), 1);
        Assert.assertEquals(item.getEasinessFactor(), 2.1f);
        Assert.assertEquals(item.getId(), "test-id");
        Assert.assertEquals(item.getDueDate(), now);
        Assert.assertEquals(item.getLastReviewedDate(), now);
    }
}
