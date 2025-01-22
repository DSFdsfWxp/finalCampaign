package finalCampaign.feature.editMode;

import arc.*;
import arc.util.*;
import finalCampaign.event.*;
import finalCampaign.feature.tuner.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;

public class fEditMode {
    public static tunerConfig config;
    private static boolean enabled;

    public static class tunerConfig {
        public workCondition workCondition = fEditMode.workCondition.selectedPartBuilding;
        public fTuner.contentChooser partOfBuilding = new fTuner.contentChooser(0, -1, uc -> uc.getContentType() == ContentType.block);
    }

    public enum workCondition {
        always,
        selectedAllBuilding,
        selectedPartBuilding
    }

    public static boolean supported() {
        return OS.isAndroid;
    }

    public static void init() {
        config = new tunerConfig();
    }

    public static void load() {
        enabled = fTuner.add("editMode", false, config, v -> enabled = v);

        Events.on(fcInputHandlePinchEvent.class, logic::pinch);
        Events.on(fcInputHandlePinchStopEvent.class, logic::pinchStop);
        Events.on(StateChangeEvent.class, logic::gameStateChange);
        Events.on(fcInputHandleUpdateEvent.class, logic::update);
        Events.on(fcInputHandlePanEvent.class, logic::pan);
        Events.on(fcInputHandleLongPressEvent.class, logic::longPress);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isOn() {
        if (!enabled)
            return false;

        return switch (config.workCondition) {
            case always -> true;
            case selectedAllBuilding -> Vars.control.input.block != null;
            case selectedPartBuilding -> Vars.control.input.block != null && config.partOfBuilding.choosedContents.contains(Vars.control.input.block);
        };
    }
}
