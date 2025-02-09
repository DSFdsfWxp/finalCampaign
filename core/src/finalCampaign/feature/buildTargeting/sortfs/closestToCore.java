package finalCampaign.feature.buildTargeting.sortfs;

import arc.math.geom.*;
import finalCampaign.feature.buildTargeting.fcSortf.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import mindustry.world.blocks.storage.CoreBlock.*;

public class closestToCore extends baseSortf<NoneConfig> {
    private Vec2 tmp;

    public closestToCore(TurretBuild build) {
        super("closestToCore", build);
        tmp = new Vec2();
    }

    public NoneConfig defaultConfig() {
        return NoneConfig.instance;
    }

    public boolean isValid() {
        return true;
    }

    public float calc(Unit unit) {
        CoreBuild core = unit.team == build.team ? unit.closestCore() : unit.closestEnemyCore();
        if (core == null)
            return 0f;
        return - tmp.set(unit.x - core.x, unit.y - core.y).len();
    }

    public float calc(Building building) {
        CoreBuild core = building.team == build.team ? building.closestCore() : building.closestEnemyCore();
        if (core == null)
            return 0f;
        return - tmp.set(building.x - core.x, building.y - core.y).len();
    }
}
