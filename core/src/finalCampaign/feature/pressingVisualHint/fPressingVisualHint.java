package finalCampaign.feature.pressingVisualHint;

import arc.*;
import arc.util.*;
import finalCampaign.event.*;
import finalCampaign.feature.setMode.*;
import finalCampaign.feature.tuner.*;
import mindustry.*;
import mindustry.input.*;

public class fPressingVisualHint {
    public static tunerConfig config;
    private static boolean enabled;

    public static class tunerConfig {
        public boolean showWhilePlacing = true;
        public boolean showWhileTargeting = true;
        public boolean showWhileShooting = true;
        public fTuner.floatSlider scale = new fTuner.floatSlider(1f, 4f, 0.1f, 0.1f);
    }

    public static boolean supported() {
        return OS.isAndroid;
    }

    public static void lateInit() {
        config = new tunerConfig();
    }

    public static void lateLoad() {
        enabled = fTuner.add("pressingVisualHint", false, config, v -> enabled = v);

        Events.on(fcDrawWorldTopEvent.class, logic::drawTop);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isOn() {
        if (!enabled)
            return false;

        if (Vars.control.input instanceof MobileInput mi && Core.input.getTouches() == 1 && !Core.scene.hasMouse()) {
            if (config.showWhilePlacing && mi.mode == PlaceMode.placing)
                return true;
            if (config.showWhileTargeting && (mi.commandMode || fSetMode.isOn()))
                return true;
            if (config.showWhileShooting && Vars.player != null && Vars.player.shooting)
                return true;
        }

        return false;
    }
}
