package finalCampaign.feature.buildTargeting.sortfs;

import arc.math.geom.*;
import finalCampaign.feature.buildTargeting.fcSortf.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class farest extends baseSortf<NoneConfig> {
    private Vec2 tmp;

    public farest(TurretBuild build) {
        super("farest", build);
        tmp = new Vec2();
    }

    public NoneConfig defaultConfig() {
        return NoneConfig.instance;
    }

    public boolean isValid() {
        return true;
    }

    public float calc(Unit unit) {
        return tmp.set(unit.x - build.x, unit.y - build.y).len();
    }

    public float calc(Building building) {
        return tmp.set(building.x - build.x, building.y - build.y).len();
    }
}
