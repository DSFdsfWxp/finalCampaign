package finalCampaign.feature;

import finalCampaign.*;
import finalCampaign.feature.featureClass.binding.*;
import finalCampaign.feature.featureClass.control.freeVision.*;
import finalCampaign.feature.featureClass.fcContentLoader.*;

public class features {
    public static void add() {

        featureLoader.add(fFcContentLoader.class);
        featureLoader.add(fBinding.class);
        featureLoader.add(fFreeVision.class);

    }
}
