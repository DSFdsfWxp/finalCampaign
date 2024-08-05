package finalCampaign.feature.featureClass.control.setMode.setFeature;

import finalCampaign.net.*;
import mindustry.game.*;
import mindustry.gen.*;

public class teamSetter extends bSelectSetter<Team> {
    public teamSetter() {
        super("teamSetter", true);
        supportMultiSelect = true;
    }

    public String transformer(Team v) {
        return v.coloredName();
    }

    public Team[] valuesProvider() {
        return Team.baseTeams;
    }

    public Team currentValue(Building building) {
        return building.team;
    }

    public void selected(Building[] selected, Team team) {
        for (Building building : selected) fcCall.setTeam(building, team);
    }
}
