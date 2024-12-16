package finalCampaign.feature.featureClass.control.setMode;

import arc.scene.ui.layout.*;
import finalCampaign.bundle.*;
import mindustry.gen.*;

public abstract class IFeature {
    public boolean supportMultiSelect;
    public String category;
    public String name;

    public abstract boolean isSupported(Building[] selected);
    public abstract void buildUI(Building[] selected, Table table, bundleNS bundleNS);
}
