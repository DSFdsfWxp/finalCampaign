package finalCampaign.feature;

import finalCampaign.*;
import finalCampaign.feature.featureClass.binding.*;
import finalCampaign.feature.featureClass.blockShortcut.*;
import finalCampaign.feature.featureClass.control.freeVision.*;
import finalCampaign.feature.featureClass.control.roulette.*;
import finalCampaign.feature.featureClass.control.setMode.*;
import finalCampaign.feature.featureClass.control.shortcut.*;
import finalCampaign.feature.featureClass.crosshair.*;
import finalCampaign.feature.featureClass.fcDesktopInput.*;
import finalCampaign.feature.featureClass.spritePacker.*;
import finalCampaign.feature.featureClass.tuner.*;
import finalCampaign.feature.featureClass.wiki.*;

public class features {
    public static void add() {

        featureLoader.add(fFcDesktopInput.class);
        
        featureLoader.add(fFreeVision.class);
        featureLoader.add(fTuner.class);
        featureLoader.add(fCrosshair.class);
        featureLoader.add(fSpritePacker.class);
        featureLoader.add(fBlockShortcut.class);
        featureLoader.add(fRoulette.class);
        featureLoader.add(fShortcut.class);
        featureLoader.add(fSetMode.class);
        featureLoader.add(fWiki.class);
        
        featureLoader.add(fBinding.class);
    }
}
