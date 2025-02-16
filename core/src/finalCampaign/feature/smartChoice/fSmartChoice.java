package finalCampaign.feature.smartChoice;

import arc.*;
import finalCampaign.event.*;
import finalCampaign.feature.tuner.*;
import mindustry.*;

public class fSmartChoice {

    protected static boolean enabled;

    public static boolean supported() {
        return !Vars.headless;
    }

    public static void lateInit() {
        ui.init();
    }

    public static void lateLoad() {
        enabled = fTuner.add("smartChoice", false, v -> enabled = v);

        Events.on(fcInputHandleUpdateEvent.class, logic::update);
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
