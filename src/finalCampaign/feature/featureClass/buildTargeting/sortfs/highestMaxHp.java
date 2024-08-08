package finalCampaign.feature.featureClass.buildTargeting.sortfs;

import finalCampaign.feature.featureClass.buildTargeting.fcSortf.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class highestMaxHp extends baseSortf<NoneConfig> {
    public highestMaxHp(TurretBuild build) {
        super("highestMaxHp", build);
    }

    public NoneConfig defaultConfig() {
        return new NoneConfig();
    }

    public boolean isValid() {
        return true;
    }

    public float calc(Unit unit) {
        return clampFloat(unit.maxHealth);
    }

    public float calc(Building building) {
        return clampFloat(building.maxHealth);
    }
}
