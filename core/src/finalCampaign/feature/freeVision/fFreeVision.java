package finalCampaign.feature.freeVision;

import arc.*;
import finalCampaign.*;
import finalCampaign.event.*;
import finalCampaign.feature.tuner.*;
import finalCampaign.input.*;
import mindustry.*;
import mindustry.game.EventType.*;

public class fFreeVision {
    
    private static boolean inited = false;
    private static boolean on;
    private static boolean enabled;
    private static config config;
    private static infoFragment lastFragment = null;

    public static class config {
        public boolean autoTargeting = true;
    }

    public static boolean supported() {
        return !Vars.headless && !Vars.mobile;
    }

    public static void init() throws Exception {
        on = setting.getAndCast("feature.control.freeVision.on", false);
        enabled = false;
        config = new config();
    }

    public static void load() throws Exception {
        Events.on(fcInputHandleUpdateEvent.class, e -> {
            checkOnOff();
            logic.update();
        });
        Events.on(StateChangeEvent.class, e -> logic.updateState());
        Events.on(fcInputHandleUpdateMovementEvent.class, e -> logic.updateMovement(e.unit));
        Events.on(fcDrawWorldOverSelectEvent.class, e -> logic.drawOverSelect());
        Events.on(fcInputHandleTapEvent.class, e -> {
            if (!e.atHead)
                logic.tap(e.x, e.y, e.count, e.button);
        });

        enabled = fTuner.add("freeVision", false, config, v -> enabled = v);

        inited = true;
    }

    public static boolean isOn() {
        return inited && on && enabled;
    }

    public static boolean autoTargetingEnabled() {
        return config.autoTargeting;
    }

    public static void checkOnOff() {
        if (Core.input.keyTap(fcBindings.freeVision) && inited && enabled) {
            on = !on;
            setting.put("feature.control.freeVision.on", on);

            if (lastFragment != null) lastFragment.remove();
            infoFragment info = new infoFragment();
            Vars.ui.hudGroup.addChild(info);
            lastFragment = info;
            info.added();
        }
    }
}
