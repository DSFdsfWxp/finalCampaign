package finalCampaign.feature.lensMode;

import arc.*;
import finalCampaign.*;
import finalCampaign.feature.tuner.*;
import finalCampaign.input.*;
import mindustry.*;

public class fLensMode {

    protected static boolean enabled;
    protected static lensMode mode;
    protected static boolean autoTargeting;
    protected static float panSpeedPercent;

    public static boolean supported() {
        return !Vars.headless;
    }

    public static void lateInit() {
        mode = lensMode.valueOf(setting.getAndCast("lensMode.mode", Vars.mobile ? "followCamera" : "defaultCamera"));
        autoTargeting = setting.getAndCast("lensMode.autoTargeting", Vars.mobile);
        panSpeedPercent = setting.getAndCast("lensMode.panSpeedPercent", 1f);
    }

    public static void lateLoad() {
        enabled = fTuner.add("lensMode", false, v -> enabled = v);

        fcInputHook.installAxisHook(logic::keyHook);
        fcInputHook.installHook(fcInputHook.inputHookPoint.pressed, logic::keyHook);
        fcInputHook.installHook(fcInputHook.inputHookPoint.released, logic::keyHook);
        fcInputHook.installHook(fcInputHook.inputHookPoint.tapped, logic::keyHook);

        featureBarButton.register();
    }

    protected static void setMode(lensMode mode) {
        if (fLensMode.mode == mode)
            return;
        fLensMode.mode = mode;
        setting.put("lensMode.mode", mode.name());
        ui.showModeChangeToast();
    }

    protected static void setAutoTargeting(boolean v) {
        if (autoTargeting == v)
            return;
        autoTargeting = v;
        setting.put("lensMode.autoTargeting", v);
        Core.settings.put("autotarget", v);
    }

    protected static void setPanSpeedPercent(float v) {
        if (panSpeedPercent == v)
            return;

        panSpeedPercent = v;
        setting.put("lensMode.panSpeedPercent", v);
    }


    public enum lensMode {
        defaultCamera,
        followCamera,
        freeCamera
    }
}
