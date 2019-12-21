package net.mattcarpenter.srs.sm2.utils;

import net.mattcarpenter.srs.sm2.TimeProvider;
import org.joda.time.DateTime;

public class MockTimeProvider implements TimeProvider {
    DateTime now;

    public DateTime getNow() {
        return now;
    }

    public void setNow(DateTime now) {
        this.now = now;
    }

    public MockTimeProvider(DateTime now) {
        this.now = now;
    }
}
