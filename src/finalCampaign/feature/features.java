package finalCampaign.feature;

import finalCampaign.*;
import finalCampaign.feature.featureClass.binding.*;
import finalCampaign.feature.featureClass.control.freeVision.*;
import finalCampaign.feature.featureClass.crosshair.*;
import finalCampaign.feature.featureClass.fcDesktopInput.*;
import finalCampaign.feature.featureClass.spritePacker.*;
import finalCampaign.feature.featureClass.fcContentLoader.*;
import finalCampaign.feature.featureClass.tuner.*;

public class features {
    public static void add() {

        featureLoader.add(fFcContentLoader.class);
        featureLoader.add(fFcDesktopInput.class);
        featureLoader.add(fBinding.class);
        featureLoader.add(fFreeVision.class);
        featureLoader.add(fTuner.class);
        featureLoader.add(fCrosshair.class);
        featureLoader.add(fSpritePacker.class);
        
    }
}
