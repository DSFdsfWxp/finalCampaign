package finalCampaign.feature.setMode.feature;

import arc.scene.ui.layout.*;
import finalCampaign.bundle.*;
import finalCampaign.net.*;
import finalCampaign.patch.*;
import mindustry.gen.*;

public class enabled extends bSelectSetter<enabled.enabledState> {
    private Table warning;

    public enabled() {
        super("enabled", false);
        supportMultiSelect = true;
    }

    @Override
    public void buildUI(Building[] selected, Table table, bundleNS bundleNS) {
        super.buildUI(selected, table, bundleNS);
        table.row();
        warning = table.table().growX().colspan(2).get();
    }

    public void selected(Building[] selected, enabledState state) {
        boolean forceDisable = state == enabledState.off;
        boolean status = state != enabledState.def;

        for (Building b : selected) {
            fcCall.setForceStatus(b, status, forceDisable);
        }

        warning.visible = status;
        warning.clear();
        if (status) warning.add(bundleNS.get("warning")).wrap().grow().pad(4f).left().get();
    }

    public enabledState[] valuesProvider() {
        return enabledState.values();
    }

    public enabledState currentValue(Building building) {
        IFcBuilding f = (IFcBuilding) building;
        if (!f.fcForceDisable() && !f.fcForceEnable()) return enabledState.def;
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
