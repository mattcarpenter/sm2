package net.mattcarpenter.srs.sm2;

import org.joda.time.DateTime;

public interface TimeProvider {
    public DateTime getNow();
}
