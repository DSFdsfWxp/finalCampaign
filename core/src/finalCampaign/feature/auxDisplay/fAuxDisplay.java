package finalCampaign.feature.auxDisplay;

import arc.*;
import finalCampaign.event.*;
import finalCampaign.feature.tuner.*;
import mindustry.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;

public class fAuxDisplay {

    protected static tunerConfig config;
    protected static boolean enabled;

    public static boolean supported() {
        return !Vars.headless;
    }

    public static void lateInit() {
        config = new tunerConfig();
    }

    public static void lateLoad() {
        enabled = fTuner.add("auxDisplay", false, config, v -> enabled = v);

        Events.on(fcPlacementFragHoveredUpdateEvent.class, logic::updateHovered);
        Events.on(fcInputHandleUpdateEvent.class, logic::update);
        Events.on(EventType.StateChangeEvent.class, logic::updateState);
        Events.on(fcDrawWorldTopEvent.class, logic::drawTop);
        Events.on(fcDrawWorldBottomEvent.class, logic::drawBottom);
        Events.on(fcEntityDisplayInfoEvent.class, logic::displayEntityInfo);
    }

    public static void addPlan(Player player, BuildPlan plan) {
        logic.plans.addPlan(player, plan);
    }

    public static void removePlan(Player player, int x, int y) {
        logic.plans.removePlan(player, x, y);
    }

    public static class tunerConfig {
        public boolean displayEnemyInfo = false;
        public boolean displayTeamPlayerBuildPlan = false;
        public boolean displayBuildCancelTip = false;
    }

}
