package finalCampaign.feature.buildTargetingLimit.filters;

import arc.util.io.*;
import finalCampaign.feature.buildTargetingLimit.fcFilter.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.game.Teams.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class notTooLessTarget extends baseFilter<Integer> {
    private boolean needReset;
    private boolean pass;
    private int count;

    public notTooLessTarget(TurretBuild build) {
        super("notTooLessTarget", build);
        needReset = false;
        pass = false;
        count = 0;
    }

    public boolean get(Unit unit) {
        checkReset();
        return pass;
    }

    public boolean get(Building building) {
        checkReset();
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

    private void checkReset() {
        if (!needReset) return;
        count = 0;

        Units.nearbyEnemies(build.team, build.x - block.range, build.y - block.range, block.range * 2f, block.range * 2f, e -> {
            if(e.dead() || e.team == Team.derelict || !e.within(build.x, build.y, block.range + e.hitSize/2f) || !e.targetable(build.team) || e.inFogTo(build.team)) return;
            if ((e.isGrounded() && !block.targetGround) || (!e.isGrounded() && !block.targetAir)) return;
            
            count ++;
        });

        for (TeamData data : Vars.state.teams.present) {
            if (data.buildingTree == null) continue;
            data.buildingTree.intersect(build.x - block.range, build.y - block.range, block.range * 2f, block.range * 2f, e -> {
                if (e != null && e.within(build.x, build.y, block.range + e.hitSize() / 2f)) {
                    if (e.dead() || (e.team == Team.derelict && !Vars.state.rules.coreCapture) || e.team == build.team || !block.targetGround) return;

                    count ++;
                }
            });
        }

        pass = count >= config;
        needReset = false;
    }
}
