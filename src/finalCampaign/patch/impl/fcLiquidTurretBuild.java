package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import finalCampaign.patch.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.defense.turrets.LiquidTurret.*;

@Mixin(LiquidTurretBuild.class)
public abstract class fcLiquidTurretBuild extends Building implements IFcLiquidTurretBuild {
    private LiquidTurretBuild turretBuild = (LiquidTurretBuild)(Object) this;
    private IFcTurretBuild fcTurretBuild = (IFcTurretBuild) this;
    private LiquidTurret turretBlock = (LiquidTurret) block;
    private boolean fcPreferExtinguish = true;

    public boolean fcPreferExtinguish() {
        return fcPreferExtinguish;
    }

    public void fcPreferExtinguish(boolean v) {
        fcPreferExtinguish = v;
    }

    protected void findTarget(){
        Runnable extinguish = () -> {
            if (turretBlock.extinguish && liquids.current().canExtinguish()) {
                float range = turretBlock.range;
    
                int tx = World.toTile(x), ty = World.toTile(y);
                Fire result = null;
                float mindst = 0f;
                int tr = (int)(range / Vars.tilesize);
                for (int x = -tr; x <= tr; x++) {
                    for (int y = -tr; y <= tr; y++) {
                        Tile other = Vars.world.tile(x + tx, y + ty);
                        var fire = Fires.get(x + tx, y + ty);
                        float dst = fire == null ? 0 : dst2(fire);
                        //do not extinguish fires on other team blocks
                        if (other != null && fire != null && Fires.has(other.x, other.y) && dst <= range * range && (result == null || dst < mindst) && (other.build == null || other.team() == team)) {
                            result = fire;
                            mindst = dst;
                        }
                    }
                }
    
                if (result != null) turretBuild.target = result;
            }
        };

        Runnable findTarget = () -> fcTurretBuild.fcFindTarget();

        if (fcPreferExtinguish) {
            extinguish.run();
            if (turretBuild.target == null) findTarget.run();
        } else {
            findTarget.run();
            if (turretBuild.target == null) extinguish.run();
        }
    }
}
