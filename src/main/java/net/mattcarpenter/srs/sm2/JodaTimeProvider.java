package net.mattcarpenter.srs.sm2;

import org.joda.time.DateTime;

public class JodaTimeProvider implements TimeProvider {
    public DateTime getNow() {
        return DateTime.now();
    }
}
