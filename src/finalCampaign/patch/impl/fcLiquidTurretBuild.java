package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.util.io.*;
import finalCampaign.feature.featureClass.mapVersion.*;
import finalCampaign.patch.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.defense.turrets.LiquidTurret.*;

@Mixin(LiquidTurretBuild.class)
public abstract class fcLiquidTurretBuild extends Building implements IFcLiquidTurretBuild {
    private LiquidTurretBuild turretBuild = (LiquidTurretBuild)(Object) this;
    private IFcTurretBuild fcTurretBuild = (IFcTurretBuild) this;
    private LiquidTurret turretBlock;
    private boolean fcPreferExtinguish = true;

    @Override
    public Building create(Block block, Team team) {
        turretBlock = (LiquidTurret) block;
        return super.create(block, team);
    }

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

    @Inject(method = "useAmmo", at = @At("RETURN"), remap = false)
    public void fcUseAmmo(CallbackInfoReturnable<BulletType> ci) {
        IFcLiquidModule fcLiquidModule = (IFcLiquidModule) liquids;
        if (liquids.currentAmount() <= 0f) fcLiquidModule.fcFindNextAvailable();
    }

    @Inject(method = "read", at = @At("RETURN"), remap = false)
    public void fcRead(Reads read, byte revision, CallbackInfo ci) {
        if (fMapVersion.currentVersion() < 1) return;
        fcPreferExtinguish = read.bool();
    }

    @Inject(method = "write", at = @At("RETURN"), remap = false)
    public void fcWrite(Writes write, CallbackInfo ci) {
        write.bool(fcPreferExtinguish);
    }
}
