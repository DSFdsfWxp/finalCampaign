package finalCampaign.feature.auxDisplay;

import finalCampaign.feature.tuner.*;
import mindustry.*;

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
    }

    public static class tunerConfig {
        public boolean displayEnemyInfo = false;
        public boolean displayTeamPlayerBuildPlan = false;
        public boolean displayBuildCancelTipAndAnimation = false;
    }

}
