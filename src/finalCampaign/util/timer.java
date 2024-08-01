package finalCampaign.util;

import arc.util.*;

public class timer {
    private long nano;
    public Object customObject;

    public timer() {
        nano = 0;
    }

    public boolean marked() {
        return nano != 0;
    }

    public void mark() {
        nano = Time.nanos();
    }

    public void unmark() {
        nano = 0;
    }

    public long msTime() {
        return Time.nanosToMillis(Time.timeSinceNanos(nano));
    }

    public float sTime() {
        return msTime() / 1000f;
    }
}
