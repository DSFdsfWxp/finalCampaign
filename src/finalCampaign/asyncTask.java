package finalCampaign;

import arc.struct.*;
import arc.util.*;

public class asyncTask {
    private static @Nullable asyncTask current = null;
    private static float defaultDelay = 10f;

    private Seq<asyncTask> subTasks;
    private @Nullable asyncTask subTask;
    private @Nullable asyncTask parent;
    private boolean interrupt;
    private boolean done;
    private Runnable task;

    public float delay;

    @Nullable
    public static asyncTask currentTask() {
        return current;
    }

    public static void reschedule() {
        if (current != null) current.schedule();
    }

    public static void reschedule(float delay) {
        if (current != null) current.schedule(delay);
    }

    public static void defaultDelay(float delay) {
        defaultDelay = delay;
    }

    public static void subTask(asyncTask task) {
        if (task == null) throw new RuntimeException("asyncTask@null is not accepted.");
        if (current != null) current.subTasks.add(task);
        task.parent = current;
    }

    public static void subTask(Runnable task) {
        subTask(new asyncTask(task));
    }

    public static void subTask(float delay, Runnable task) {
        subTask(new asyncTask(delay, task));
    }

    public static void subTask(Thread thread) {
        subTask(new asyncTask(thread, null, true));
    }

    public static void interrupt() {
        if (current != null) current.interrupt = true;
    }

    public asyncTask(Thread thread, Runnable then) {
        this(thread, new asyncTask(then), true);
    }

    public asyncTask(Thread thread, @Nullable asyncTask then, boolean autoStart) {
        this(() -> {
            switch (thread.getState()) {
                case TERMINATED:
                    if (then != null) then.schedule();
                    break;
                case NEW:
                    if (autoStart) thread.start();
                default: 
                    reschedule();
            }
        });
    }

    public asyncTask(Runnable task) {
        this(defaultDelay, task);
    }

    public asyncTask(float delay, Runnable task) {
        done = false;
        subTasks = new Seq<>();
        parent = null;

        this.delay = delay;
        this.task = () -> {

            current = this;
            interrupt = false;
            
            task.run();
            
            if (!interrupt && subTasks.size > 0) {
                subTask = subTasks.first();
                subTask.schedule();
            } else {
                done = !interrupt;
                asyncTask parent = this.parent;

                while (parent != null && !interrupt) {
                    Seq<asyncTask> subTasks = parent.subTasks;
                    int pos = subTasks.indexOf(parent.subTask);

                    if (pos >= 0 && pos + 1 < subTasks.size) {
                        asyncTask next = subTasks.get(pos + 1);
                        parent.subTask = next;
                        next.schedule();
                        break;
                    } else {
                        parent.subTask = null;
                        parent.done = true;
                        parent = interrupt ? null : parent.parent;
                    }
                }
            }

            current = null;
        };
    }

    public boolean done() {
        return done;
    }

    @Nullable
    public asyncTask parent() {
        return parent;
    }

    public void schedule() {
        schedule(this.delay);
    }

    public void schedule(float delay) {
        if (done) return;

        subTasks.clear();
        subTask = null;
        interrupt = true;
        Time.run(delay, task);
    }

    
}
