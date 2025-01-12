package finalCampaign.feature.buildTargeting.sortfs;

import arc.math.geom.*;
import finalCampaign.feature.buildTargeting.fcSortf.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class farest extends baseSortf<NoneConfig> {
    public farest(TurretBuild build) {
        super("farest", build);
    }

    public NoneConfig defaultConfig() {
        return new NoneConfig();
    }

    public boolean isValid() {
        return true;
    }

    public float calc(Unit unit) {
        return (new Vec2(unit.x - build.x, unit.y - build.y)).len();
    }

    public float calc(Building building) {
        return (new Vec2(building.x - build.x, building.y - build.y)).len();
    }
}
