package finalCampaign.feature.featureClass.control.setMode.setFeature;

import finalCampaign.net.*;
import mindustry.gen.*;

public class healthSetter extends bBarSetter {
    public healthSetter() {
        super("healthSetter", true, 1, 0, 0, false, true, false, false, true);
        supportMultiSelect = true;
    }

    public void change(Building[] selected, float value) {
        for (Building building : selected)
            fcCall.setHealth(building, building.maxHealth * value);
    }

    public boolean init(Building[] selected) {
        return selected.length > 1;
    }
    
}
