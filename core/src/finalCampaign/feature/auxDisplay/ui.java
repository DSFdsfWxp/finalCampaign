package finalCampaign.feature.auxDisplay;

import finalCampaign.*;
import finalCampaign.feature.hudUI.*;

public class ui {

    private static Runnable closeToast;

    public static void showToast(boolean build) {
        if (closeToast != null)
            return;

        fHudUI.showBottomToast((t, close) -> {
            t.add(bundle.get(build ? "auxDisplay.cancelBuild" : "auxDisplay.cancelBreak"));
            closeToast = close;
        });
    }

    public static void closeToast() {
        if (closeToast == null)
            return;

        closeToast.run();
        closeToast = null;
    }
}
