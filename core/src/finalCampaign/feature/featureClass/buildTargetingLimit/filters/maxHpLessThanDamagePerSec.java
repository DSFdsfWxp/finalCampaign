package finalCampaign.feature.featureClass.buildTargetingLimit.filters;

import finalCampaign.feature.featureClass.buildTargetingLimit.fcFilter.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class maxHpLessThanDamagePerSec extends baseFilter<NoneConfig> {
    public maxHpLessThanDamagePerSec(TurretBuild build) {
        super("maxHpLessThanDamagePerSec", build);
    }

    public boolean get(Unit unit) {
        return build.peekAmmo().estimateDPS() >= unit.maxHealth;
    }

    public boolean get(Building building) {
        return build.peekAmmo().estimateDPS() >= building.maxHealth;
    }

    public NoneConfig defaultConfig() {
        return new NoneConfig();
    }
}
