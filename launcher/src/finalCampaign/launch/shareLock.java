package finalCampaign.launch;

import java.util.concurrent.locks.*;
import arc.func.*;

public class shareLock {
    private ReentrantLock lock;

    public static interface lockAction {
        public void run() throws Throwable;
    }

    public shareLock() {
        lock = new ReentrantLock();
    }

    public void run(lockAction action) {
        Throwable err = null;
        lock.lock();
        try {
            action.run();
        } catch(Throwable e) {
            err = e;
        } finally {
            lock.unlock();
            if (err != null) throw new RuntimeException(err);
        }
    }

    public <T extends Throwable> void run(lockAction action, Func<Throwable, T> errConstuctor) throws T {
        Throwable err = null;
        lock.lock();
        try {
            action.run();
        } catch(Throwable e) {
            err = e;
        } finally {
            lock.unlock();
            if (err != null) throw errConstuctor.get(err);
        }
    }
}
