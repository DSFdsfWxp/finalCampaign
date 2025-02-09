package finalCampaign.feature.featureBar;

import arc.*;
import finalCampaign.event.*;
import mindustry.*;

public class fFeatureBar {

    public static boolean supported() {
        return !Vars.headless;
    }

    public static void lateInit() {

    }

    public static void lateLoad() {

    }

    public static void earlyLoad() {
        Events.on(fcInputHandleBuildPlacementUIEvent.class, logic::buildPlacementUI);
    }
}
