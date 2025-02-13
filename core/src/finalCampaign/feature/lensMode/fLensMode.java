package finalCampaign.feature.lensMode;

import arc.*;
import finalCampaign.*;
import finalCampaign.feature.tuner.*;
import mindustry.*;

public class fLensMode {

    protected static boolean enabled;
    protected static lensMode mode;
    protected static boolean autoTargeting;

    public static boolean supported() {
        return !Vars.headless;
    }

    public static void lateInit() {
        mode = lensMode.valueOf(setting.getAndCast("lensMode.mode", Vars.mobile ? "followCamera" : "defaultCamera"));
        autoTargeting = setting.getAndCast("lensMode.autoTargeting", false);
    }

    public static void lateLoad() {
        enabled = fTuner.add("lensMode", false, v -> enabled = v);
        featureBarButton.register();
    }

    protected static void setMode(lensMode mode) {
        if (fLensMode.mode == mode)
            return;
        fLensMode.mode = mode;
        setting.put("lensMode.mode", mode.name());
    }

    protected static void setAutoTargeting(boolean v) {
        if (autoTargeting == v)
            return;
        autoTargeting = v;
        setting.put("lensMode.autoTargeting", v);
        Core.settings.put("autotarget", v);
    }


    public enum lensMode {
        defaultCamera,
        followCamera,
        freeCamera
    }
}
