package finalCampaign.feature.buildTargeting.sortfs;

import finalCampaign.feature.buildTargeting.fcSortf.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class uneffected extends baseSortf<NoneConfig> {
    private StatusEffect status;
    public uneffected(TurretBuild build) {
        super("uneffected", build);
        BulletType type = build.peekAmmo();
        status = type == null ? StatusEffects.none : type.status;
    }

    public NoneConfig defaultConfig() {
        return NoneConfig.instance;
    }

    public boolean isValid() {
        return true;
    }

    public float calc(Unit unit) {
        return unit.hasEffect(status) ? 0f : 1f;
    }

    public float calc(Building building) {
        // wait to design
        return 0f;
    }
}
