package finalCampaign.feature.featureClass.buildTargetingLimit.filters;

import finalCampaign.feature.featureClass.buildTargetingLimit.fcFilter.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class buildingOnly extends baseFilter<NoneConfig> {
    public buildingOnly(TurretBuild build) {
        super("buildingOnly", build);
    }

    public boolean get(Unit unit) {
        return false;
    }

    public boolean get(Building building) {
        return true;
    }

    public NoneConfig defaultConfig() {
        return new NoneConfig();
    }
}
