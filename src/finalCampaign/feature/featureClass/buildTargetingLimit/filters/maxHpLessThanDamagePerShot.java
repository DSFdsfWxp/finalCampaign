package finalCampaign.feature.featureClass.buildTargetingLimit.filters;

import finalCampaign.feature.featureClass.buildTargetingLimit.fcFilter.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class maxHpLessThanDamagePerShot extends baseFilter<NoneConfig> {
    public maxHpLessThanDamagePerShot(TurretBuild build) {
        super("maxHpLessThanDamagePerShot", build);
    }

    public boolean get(Unit unit) {
        return build.peekAmmo().damage >= unit.maxHealth;
    }

    public boolean get(Building building) {
        return build.peekAmmo().damage >= building.maxHealth;
    }

    public NoneConfig defaultConfig() {
        return new NoneConfig();
    }
}
