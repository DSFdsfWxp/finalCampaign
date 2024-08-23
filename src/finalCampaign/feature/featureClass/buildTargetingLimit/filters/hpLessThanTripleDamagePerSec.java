package finalCampaign.feature.featureClass.buildTargetingLimit.filters;

import finalCampaign.feature.featureClass.buildTargetingLimit.fcFilter.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class hpLessThanTripleDamagePerSec extends baseFilter<NoneConfig> {
    public hpLessThanTripleDamagePerSec(TurretBuild build) {
        super("hpLessThanTripleDamagePerSec", build);
    }

    public boolean get(Unit unit) {
        return build.peekAmmo().estimateDPS() * 3f >= unit.health;
    }

    public boolean get(Building building) {
        return build.peekAmmo().estimateDPS() * 3f >= building.health;
    }

    public NoneConfig defaultConfig() {
        return new NoneConfig();
    }
}
