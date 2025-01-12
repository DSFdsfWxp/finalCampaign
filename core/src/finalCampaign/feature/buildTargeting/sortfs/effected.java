package finalCampaign.feature.buildTargeting.sortfs;

import finalCampaign.feature.buildTargeting.fcSortf.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class effected extends baseSortf<NoneConfig> {
    public effected(TurretBuild build) {
        super("effected", build);
    }

    public NoneConfig defaultConfig() {
        return new NoneConfig();
    }

    public boolean isValid() {
        return true;
    }

    public float calc(Unit unit) {
        return unit.statusBits().isEmpty() ? 0f: 1f;
    }

    public float calc(Building building) {
        // wait to design
        return 0f;
    }
}
