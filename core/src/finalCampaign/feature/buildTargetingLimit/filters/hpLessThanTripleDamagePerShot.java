package finalCampaign.feature.buildTargetingLimit.filters;

import finalCampaign.feature.buildTargetingLimit.fcFilter.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class hpLessThanTripleDamagePerShot extends baseFilter<NoneConfig> {
    public hpLessThanTripleDamagePerShot(TurretBuild build) {
        super("hpLessThanTripleDamagePerShot", build);
    }

    public boolean get(Unit unit) {
        return build.peekAmmo().damage * 3f >= unit.health;
    }

    public boolean get(Building building) {
        return build.peekAmmo().damage * 3f >= building.health;
    }

    public NoneConfig defaultConfig() {
        return new NoneConfig();
    }
}
