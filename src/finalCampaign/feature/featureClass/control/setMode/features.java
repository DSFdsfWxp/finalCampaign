package finalCampaign.feature.featureClass.control.setMode;

import finalCampaign.feature.featureClass.control.setMode.setFeature.*;

public class features {
    public static void add() {
        // basic
        fSetMode.addFeature(new statusInfo());
        fSetMode.addFeature(new basicInfo());

        // content
        fSetMode.addFeature(new itemStack());
    }
}
