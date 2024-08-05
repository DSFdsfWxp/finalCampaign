package finalCampaign.feature.featureClass.control.setMode.setFeature;

import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import finalCampaign.bundle.*;
import finalCampaign.feature.featureClass.control.setMode.*;
import finalCampaign.net.*;
import finalCampaign.ui.*;
import finalCampaign.util.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;

public class teamSetter extends iFeature {
    public teamSetter() {
        category = "setting";
        name = "teamSetter";
        supportMultiSelect = true;
    }

    public boolean isSupported(Building[] selected) {
        return Vars.state.rules.mode() == Gamemode.sandbox;
    }

    public void buildUI(Building[] selected, Table table, bundleNS bundleNS) {
        table.add(bundleNS.get("name")).width(100f).left();
        TextButton button = new TextButton("");
        Team team = selected[0].team;
        boolean ambiguous = false;
        for (Building building : selected) if (building.team != team) ambiguous = true;
        String[] lst = new String[Team.baseTeams.length];
        for (int i=0; i<lst.length; i++) lst[i] = Team.baseTeams[i].coloredName();

        fakeFinal<String> current = new fakeFinal<>(ambiguous ? "..." : team.coloredName());
        button.clicked(() -> {
            selectTable.showSelect(button, lst, current.get(), t -> {
                button.setText(t);
                int id = 0;
                for (id=0; id<lst.length; id++) if (lst[id].equals(t)) break;
                Team selectedTeam = Team.get(id);
                for (Building building : selected) fcCall.setTeam(building, selectedTeam);
            });
        });
    }
}
