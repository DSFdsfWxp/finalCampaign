package finalCampaign.ui.action;

import arc.scene.*;

public class runnableAction extends Action {
    private Runnable run;

    public runnableAction(Runnable task) {
        run = task;
    }

    public boolean act(float var1) {
        run.run();
        return true;
    }
}
