package finalCampaign.feature.buildTargetingLimit.filters;

import finalCampaign.feature.buildTargetingLimit.fcFilter.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class unitOnly extends baseFilter<NoneConfig> {
    public unitOnly(TurretBuild build) {
        super("unitOnly", build);
    }

    public boolean get(Unit unit) {
        return true;
    }

    public boolean get(Building building) {
        return false;
    }

    public NoneConfig defaultConfig() {
        return new NoneConfig();
    }
}
