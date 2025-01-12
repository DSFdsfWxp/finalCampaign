package finalCampaign.feature.setMode;

import finalCampaign.feature.setMode.feature.*;

public class features {
    public static void add() {
        // basic
        fSetMode.addFeature(new statusInfo());
        fSetMode.addFeature(new basicInfo());

        // content
        fSetMode.addFeature(new itemStack());
        fSetMode.addFeature(new multiItemStack());

        // setting
        fSetMode.addFeature(new enabled());
        fSetMode.addFeature(new teamSetter());
        fSetMode.addFeature(new healthSetter());
        fSetMode.addFeature(new predictTarget());
        fSetMode.addFeature(new targetingPriority());
        fSetMode.addFeature(new targetingLimit());
        fSetMode.addFeature(new drillPreferItem());
    }
}
