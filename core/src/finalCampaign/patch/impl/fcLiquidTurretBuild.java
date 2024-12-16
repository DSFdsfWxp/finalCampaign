package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.util.io.*;
import finalCampaign.net.*;
import finalCampaign.patch.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.LiquidTurret.*;

@Mixin(LiquidTurretBuild.class)
public abstract class fcLiquidTurretBuild extends Building implements IFcLiquidTurretBuild {
    private LiquidTurretBuild turretBuild = (LiquidTurretBuild)(Object) this;
    private IFcTurretBuild fcTurretBuild = (IFcTurretBuild) this;
    private IFcLiquidTurret fcTurret;
    private boolean fcPreferExtinguish = true;

    @Override
    public Building create(Block block, Team team) {
        fcTurret = (IFcLiquidTurret) block;
        return super.create(block, team);
    }

    @Override
    public void playerPlaced(Object config) {
        super.playerPlaced(config);
        fcCall.setTurretPreferExtinguish(this, fcTurret.fcPreferExtinguish());
    }

    public boolean fcPreferExtinguish() {
        return fcPreferExtinguish;
    }

    public void fcPreferExtinguish(boolean v) {
        fcPreferExtinguish = v;
    }

    @Inject(method = "findTarget", at = @At("HEAD"), remap = false, cancellable = true)
    private void fcFindTargetBefore(CallbackInfo ci) {
        if (!fcPreferExtinguish) {
            fcTurretBuild.fcFindTarget();
            if (turretBuild.target != null)
                ci.cancel();
        }
    }

    @Inject(method = "findTarget", at = @At("RETURN"), remap = false)
    private void fcFindTargetAfter(CallbackInfo ci) {
        if (fcPreferExtinguish && turretBuild.target == null)
            fcTurretBuild.fcFindTarget();
    }

    @Inject(method = "useAmmo", at = @At("RETURN"), remap = false)
    public void fcUseAmmo(CallbackInfoReturnable<BulletType> ci) {
        IFcLiquidModule fcLiquidModule = (IFcLiquidModule) liquids;
        if (liquids.currentAmount() <= 0f) fcLiquidModule.fcFindNextAvailable();
    }

    @Override
    public void read(Reads read, byte revision) {
        super.read(read, revision);
        if (finalCampaign.map.fcMap.currentVersion < 2) return;
        fcPreferExtinguish = read.bool();
    }

    @Override
    public void write(Writes write) {
        super.write(write);
        write.bool(fcPreferExtinguish);
    }
}
