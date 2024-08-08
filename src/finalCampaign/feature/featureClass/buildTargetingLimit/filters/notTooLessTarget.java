package finalCampaign.feature.featureClass.buildTargetingLimit.filters;

import arc.util.io.*;
import finalCampaign.feature.featureClass.buildTargetingLimit.fcFilter.*;
import finalCampaign.util.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class notTooLessTarget extends baseFilter<Integer> {
    private boolean needReset;
    private boolean pass;

    public notTooLessTarget(TurretBuild build) {
        super("notTooLessTarget", build);
        needReset = false;
        pass = false;
    }

    public boolean get(Unit unit) {
        checkReset(true);
        return pass;
    }

    public boolean get(Building building) {
        checkReset(false);
        return pass;
    }

    public Integer defaultConfig() {
        return 6;
    }

    @Override
    public void read(Reads reads) {
        config = reads.i();
    }

    @Override
    public void write(Writes writes) {
        writes.i(config);
    }

    @Override
    public void beforeTargeting() {
        needReset = true;
        pass = false;
    }

    private void checkReset(boolean targetUnit) {
        if (!needReset) return;

        fakeFinal<Integer> count = new fakeFinal<>(0);
        if (targetUnit) {
            Units.nearbyEnemies(build.team, build.x - block.range, build.y - block.range, block.range*2f, block.range*2f, e -> {
                if(e.dead() || e.team == Team.derelict || !e.within(build.x, build.y, block.range + e.hitSize/2f) || !e.targetable(build.team) || e.inFogTo(build.team)) return;
                if ((e.isGrounded() && !block.targetGround) || (!e.isGrounded() && !block.targetAir)) return;
                
                count.set(count.get() + 1);
            });
        } else {
            Vars.indexer.allBuildings(build.x, build.y, block.range, e -> {
                if (e.dead() || (e.team == Team.derelict && !Vars.state.rules.coreCapture) || e.team == build.team || !block.targetGround) return;

                count.set(count.get() + 1);
            });
        }

        pass = count.get() > config;

        needReset = false;
    }
}
