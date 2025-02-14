package finalCampaign.feature.lensMode;

import finalCampaign.*;
import finalCampaign.feature.hudUI.*;

public class ui {

    private static Runnable closeSpeedSetToast;

    public static void makeSureSpeedSetToastOpen() {
        if (closeSpeedSetToast == null)
            fHudUI.showBottomToast((t, close) -> {
                closeSpeedSetToast = close;
                t.label(() -> bundle.format("lensMode.speedSetToast", Integer.toString((int) Math.ceil(fLensMode.panSpeedPercent * 100f))));
            });
    }

    public static void closeSpeedSetToast() {
        if (closeSpeedSetToast != null) {
            closeSpeedSetToast.run();
            closeSpeedSetToast = null;
        }
    }

    public static void showModeChangeToast() {
        fHudUI.showBottomToast(bundle.get("lensMode." + fLensMode.mode.name() + ".name"));
    }
}
