package finalCampaign.feature.featureClass.control.setMode.setFeature;

import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import finalCampaign.bundle.*;
import finalCampaign.feature.featureClass.control.setMode.*;
import finalCampaign.net.*;
import finalCampaign.patch.*;
import finalCampaign.ui.*;
import finalCampaign.util.*;
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
        Building firstBuilding = selected[0];
        IFcBuilding fcFristBuilding = (IFcBuilding) firstBuilding;
        table.add(bundleNS.get("name")).width(100f).left();
        TextButton button = table.button("null", () -> {}).width(50f).right().get();
        table.row();
        Label warning = table.add(bundleNS.get("warning")).colspan(2).center().visible(false).get();
        boolean forceStatus = fcFristBuilding.fcForceDisable() || fcFristBuilding.fcForceEnable();
        boolean ambiguous = false;
        fakeFinal<String> current = new fakeFinal<>();

        for (Building b : selected) {
            IFcBuilding fcB = (IFcBuilding) b;
            if (fcB.fcForceDisable() || fcB.fcForceEnable()) {
                if (forceStatus) {
                    if (fcB.fcForceDisable() != fcFristBuilding.fcForceDisable()) ambiguous = true;
                } else {
                    ambiguous = true;
                }
            } else {
                if (forceStatus) ambiguous = true;
            }
        }
        if (ambiguous) {
            button.setText("...");
            current.set("...");
        } else {
            current.set(forceStatus ? (fcFristBuilding.fcForceDisable() ? "@off" : "@on") : "@default");
            button.setText(bundleNS.get(current.get()));
        }

        button.clicked(() -> {
            String[] lst = new String[] {"@off", "@on", "@default"};
            selectTable.showSelect(button, lst, current.get(), c -> {
                current.set(c);
                button.setText(bundleNS.get(c));
                boolean forceDisable = false;
                boolean forceEnable = false;
                if (c.equals("@off")) forceDisable = true;
                if (c.equals("@on")) forceEnable = true;
                boolean status = forceDisable || forceEnable;

                for (Building b : selected) {
                    fcCall.setForceStatus(b, status, forceDisable);
                }

                warning.visible = status;
            });
        });
    }
}
