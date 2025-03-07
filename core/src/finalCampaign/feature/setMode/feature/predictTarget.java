package finalCampaign.feature.setMode.feature;

import finalCampaign.net.*;
import finalCampaign.patch.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class predictTarget extends bSelectSetter<predictTarget.predictTargetState> {
    public predictTarget() {
        super("predictTarget", false);
        supportMultiSelect = true;
    }

    @Override
    public boolean isSupported(Building[] selected) {
        for (Building building : selected) if (building instanceof TurretBuild) return true;
        return false;
    }

    public void selected(Building[] selected, predictTargetState state) {
        for (Building building : selected) if (building instanceof TurretBuild) fcCall.setBuildingForceDisablePredictTarget(building, state == predictTargetState.off);
    }

    public String transformer(predictTargetState v) {
        return bundleNS.get(v.name());
    }

    public predictTargetState[] valuesProvider() {
        return predictTargetState.values();
    }

    public predictTargetState currentValue(Building building) {
        if (!(building instanceof IFcTurretBuild tb)) return predictTargetState.def;
        return tb.fcForceDisablePredictTarget() ? predictTargetState.off : predictTargetState.def;
    }

    public enum predictTargetState {
        off,
        def
    }
}
