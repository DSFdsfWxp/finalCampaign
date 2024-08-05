package finalCampaign.feature.featureClass.control.setMode.setFeature;

import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import finalCampaign.bundle.*;
import finalCampaign.net.*;
import finalCampaign.patch.*;
import mindustry.gen.*;

public class enabled extends bSelectSetter<enabled.enabledState> {
    private Label warning;

    public enabled() {
        super("name", false);
        supportMultiSelect = true;
    }

    @Override
    public void buildUI(Building[] selected, Table table, bundleNS bundleNS) {
        super.buildUI(selected, table, bundleNS);
        table.row();
        warning = table.add(bundleNS.get("warning")).colspan(2).center().visible(false).get();
    }

    public void selected(Building[] selected, enabledState state) {
        boolean forceDisable = state == enabledState.off;
        boolean forceEnable = state == enabledState.on;
        boolean status = forceDisable || forceEnable;

        for (Building b : selected) {
            fcCall.setForceStatus(b, status, forceDisable);
        }

        warning.visible = status;
    }

    public enabledState[] valuesProvider() {
        return enabledState.values();
    }

    public enabledState currentValue(Building building) {
        IFcBuilding f = (IFcBuilding) building;
        if (!f.fcForceDisable() || !f.fcForceEnable()) return enabledState.def;
        if (f.fcForceDisable()) return enabledState.off;
        if (f.fcForceEnable()) return enabledState.on;
        return enabledState.def;
    }

    public String transformer(enabledState state) {
        return bundleNS.get(state.name());
    }

    public static enum enabledState {
        on,
        off,
        def
    }
}
