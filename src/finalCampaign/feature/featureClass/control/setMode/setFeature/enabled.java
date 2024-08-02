package finalCampaign.feature.featureClass.control.setMode.setFeature;

import arc.scene.ui.layout.*;
import finalCampaign.bundle.*;
import finalCampaign.feature.featureClass.control.setMode.*;
import mindustry.gen.*;

public class enabled extends iFeature {
    public enabled() {
        category = "setting";
        name = "enabled";
        supportMultiSelect = true;
    }

    public boolean isSupported(Building[] selected) {
        return true;
    }

    public void buildUI(Building[] selected, Table table, bundleNS bundleNS) {
        
    }
}
