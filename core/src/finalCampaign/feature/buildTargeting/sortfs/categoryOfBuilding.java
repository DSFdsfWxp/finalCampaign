package finalCampaign.feature.buildTargeting.sortfs;

import arc.util.io.*;
import finalCampaign.feature.buildTargeting.fcSortf.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class categoryOfBuilding extends baseSortf<Category> {
    public categoryOfBuilding(TurretBuild build) {
        super("categoryOfBuilding", build);
    }

    public Category defaultConfig() {
        return Category.distribution;
    }

    public boolean isValid() {
        return !unitSide;
    }

    @Override
    public void read(Reads reads) {
        config = Category.valueOf(reads.str());
    }

    @Override
    public void write(Writes writes) {
        writes.str(config.name());
    }

    public float calc(Unit unit) {
        return 0f;
    }

    public float calc(Building building) {
        return building.block.category.equals(config) ? 1f : 0f;
    }


}
