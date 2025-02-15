package finalCampaign.feature.lensMode;

import arc.*;
import finalCampaign.*;
import finalCampaign.event.*;
import finalCampaign.feature.tuner.*;
import finalCampaign.input.*;
import mindustry.*;
import mindustry.game.*;

public class fLensMode {

    protected static boolean enabled;
    protected static lensMode mode;
    protected static boolean autoTargeting;
    protected static boolean roamingBuild;
    protected static float panSpeedPercent;

    public static boolean supported() {
        return !Vars.headless;
    }

    public static void lateInit() {
        mode = lensMode.valueOf(setting.getAndCast("lensMode.mode", Vars.mobile ? "followCamera" : "defaultCamera"));
        autoTargeting = setting.getAndCast("lensMode.autoTargeting", Vars.mobile);
        panSpeedPercent = setting.getAndCast("lensMode.panSpeedPercent", 1f);
        roamingBuild = setting.getAndCast("lensMode.roamingBuild", false);
    }

    public static void lateLoad() {
        enabled = fTuner.add("lensMode", false, v -> enabled = v);

        fcInputHook.installAxisHook(logic::keyHook);
        fcInputHook.installHook(fcInputHook.inputHookPoint.pressed, logic::keyHook);
        fcInputHook.installHook(fcInputHook.inputHookPoint.released, logic::keyHook);
        fcInputHook.installHook(fcInputHook.inputHookPoint.tapped, logic::keyHook);

        Events.on(fcInputHandleUpdateEvent.class, e -> {
            if (e.beforeUpdate)
                logic.updateBefore();
            else
                logic.updateAfter();
        });
        Events.on(fcInputHandleUpdateMovementEvent.class, e -> {
            if (e.beforeUpdate)
                logic.updateMovementBefore(e.unit);
            else
                logic.updateMovementAfter(e.unit);
        });
        Events.on(fcInputHandleTapEvent.class, e -> {
            if (!e.beforeTap)
                logic.tap(e.x, e.y, e.count, e.button);
        });
        Events.on(EventType.StateChangeEvent.class, e -> logic.updateState());
        Events.on(fcDrawWorldOverSelectEvent.class, e -> logic.drawOverSelect());

        featureBarButtons.register();
        ui.initRoulette();
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
        if (Vars.mobile)
            Core.settings.put("autotarget", v);
    }

    public static void setRoamingBuild(boolean v) {
        if (roamingBuild == v)
            return;
        roamingBuild = v;
        setting.put("lensMode.roamingBuild", v);
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
