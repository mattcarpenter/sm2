package net.mattcarpenter.srs.sm2;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JodaTimeProviderTest {

    private static final int COMPARISON_MAX_TOLERANCE_MILLIS = 100;

    @Test
    public void getNow_ok() {
        JodaTimeProvider tp = new JodaTimeProvider();
        DateTime providerNow = tp.getNow();
        DateTime testNow = DateTime.now();

        Assert.assertTrue(new Duration(providerNow, testNow).isShorterThan(Duration.millis(COMPARISON_MAX_TOLERANCE_MILLIS)));
    }
}
