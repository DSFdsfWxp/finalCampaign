package finalCampaign.util;

import arc.util.*;
import arc.util.pooling.Pool.*;
import arc.util.pooling.*;

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

    public void reset() {
        nano = 0;
    }

    public long msTime() {
        return Time.nanosToMillis(Time.timeSinceNanos(nano));
    }

    public float sTime() {
        return msTime() / 1000f;
    }

    public static task run(float delay, Runnable then) {
        task task =  Pools.obtain(task.class, task::new);
        task.delay = delay;
        task.canceled = false;
        task.finish = then;
        task.start();
        return task;
    }

    public static class task implements Poolable {
        float delay;
        Runnable finish;
        boolean canceled;

        public task() {}

        public task(float delay, Runnable then) {
            this.delay = delay;
            finish = then;
            canceled = false;
        }

        private void finish() {
            if (!canceled && finish != null) finish.run();
            Pools.free(this);
        }

        public void cancel() {
            canceled = true;
        }

        @Override
        public void reset() {
            delay = -1f;
            finish = null;
            canceled = false;
        }

        protected void start() {
            Time.run(delay, this::finish);
        }
    }
}
