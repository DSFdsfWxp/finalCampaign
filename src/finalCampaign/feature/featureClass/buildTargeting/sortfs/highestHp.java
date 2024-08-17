package finalCampaign.feature.featureClass.buildTargeting.sortfs;

import finalCampaign.feature.featureClass.buildTargeting.fcSortf.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class highestHp extends baseSortf<NoneConfig> {
    public highestHp(TurretBuild build) {
        super("highestHp", build);
    }

    public NoneConfig defaultConfig() {
        return new NoneConfig();
    }

    public boolean isValid() {
        return true;
    }

    public float calc(Unit unit) {
        return unit.health;
    }

    public float calc(Building building) {
        return building.health;
    }
}
