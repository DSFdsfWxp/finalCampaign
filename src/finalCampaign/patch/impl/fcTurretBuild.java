package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import finalCampaign.patch.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

@Mixin(TurretBuild.class)
public abstract class fcTurretBuild extends Building implements ControlBlock, IFcTurretBuild {
    @Shadow(remap = false)
    public abstract boolean hasAmmo();
    @Shadow(remap = false)
    public Vec2 targetPos;
    @Shadow(remap = false)
    public @Nullable Posc target;

    @Shadow(remap = false)
    public abstract float range();
    @Shadow(remap = false)
    protected abstract boolean canHeal();

    private Turret turretBlock = (Turret) this.block;
    private boolean fcForceDisablePredictTarget = false;
    private boolean fcPreferBuildingTarget = false;

    public boolean fcForceDisablePredictTarget() {
        return fcForceDisablePredictTarget;
    }

    public void fcForceDisablePredictTarget(boolean v) {
        fcForceDisablePredictTarget = v;
    }

    public boolean fcPreferBuildingTarget() {
        return fcPreferBuildingTarget;
    }

    public void fcPreferBuildingTarget(boolean v) {
        fcPreferBuildingTarget = v;
    }

    @Override
    public Building create(Block block, Team team) {

        return super.create(block, team);
    }

    @Inject(method = "targetPosition", at = @At("HEAD"), remap = false, cancellable = true)
    public void fcTargetPosition(Posc pos, CallbackInfo ci){
        if(!hasAmmo() || pos == null) return;
        if (!fcForceDisablePredictTarget) return;

        targetPos.set(pos);

        if(targetPos.isZero()){
            targetPos.set(pos);
        }

        ci.cancel();
    }

    @Inject(method = "read", at = @At("RETURN"), remap = false)
    public void fcRead(Reads read, byte revision, CallbackInfo ci) {
        fcForceDisablePredictTarget = read.bool();
        fcPreferBuildingTarget = read.bool();
    }

    @Inject(method = "write", at = @At("RETURN"), remap = false)
    public void fcWrite(Writes write) {
        write.bool(fcForceDisablePredictTarget);
        write.bool(fcPreferBuildingTarget);
    }

    protected void findTarget() {
        float range = range();

        Runnable findUnitTarget = () -> {
            if (turretBlock.targetAir && !turretBlock.targetGround) {
                target = Units.bestEnemy(team, x, y, range, e -> !e.dead() && !e.isGrounded() && turretBlock.unitFilter.get(e), turretBlock.unitSort);
            } else {
                target = Units.bestTarget(team, x, y, range, e -> !e.dead() && turretBlock.unitFilter.get(e) && (e.isGrounded() || turretBlock.targetAir) && (!e.isGrounded() || turretBlock.targetGround), b -> false, turretBlock.unitSort);
            }
        };

        Runnable findBuildingTarget = () -> {
            target = Units.bestTarget(team, x, y, range, e -> false, b -> turretBlock.targetGround && turretBlock.buildingFilter.get(b), turretBlock.unitSort);
            if (target == null) target = canHeal() ? Units.findAllyTile(team, x, y, range, b -> b.damaged() && b != this) : null;
        };

        if (fcPreferBuildingTarget) {
            findBuildingTarget.run();
            if (target == null) findUnitTarget.run();
        } else {
            findUnitTarget.run();
            if (target == null) findBuildingTarget.run();
        }
        
    }
}