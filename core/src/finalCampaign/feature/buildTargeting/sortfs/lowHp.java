package finalCampaign.feature.buildTargeting.sortfs;

import finalCampaign.feature.buildTargeting.fcSortf.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class lowHp extends baseSortf<NoneConfig> {
    public lowHp(TurretBuild build) {
        super("lowHp", build);
    }

    public NoneConfig defaultConfig() {
        return new NoneConfig();
    }

    public boolean isValid() {
        return true;
    }

    public float calc(Unit unit) {
        return unit.healthf() > 0.1f ? 0f: 1f;
    }

    public float calc(Building building) {
        return building.healthf() > 0.1f ? 0f: 1f;
    }
}
