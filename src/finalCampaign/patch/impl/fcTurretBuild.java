package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import finalCampaign.feature.featureClass.buildTargeting.*;
import finalCampaign.feature.featureClass.buildTargetingLimit.*;
import finalCampaign.feature.featureClass.mapVersion.*;
import finalCampaign.patch.*;
import mindustry.*;
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

    private Turret turretBlock;
    private boolean fcForceDisablePredictTarget = false;
    private boolean fcPreferBuildingTarget = false;
    private fcSortf fcSortf = new fcSortf((TurretBuild)(Object) this);
    private fcFilter fcFilter = new fcFilter((TurretBuild)(Object) this);

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

    public fcSortf fcSortf() {
        return fcSortf;
    }

    public fcFilter fcFilter() {
        return fcFilter;
    }

    @Override
    public Building create(Block block, Team team) {
        turretBlock = (Turret) block;
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
        if (fMapVersion.currentVersion() < 1) return;
        fcForceDisablePredictTarget = read.bool();
        fcPreferBuildingTarget = read.bool();
        fcSortf.read(read);
    }

    @Inject(method = "write", at = @At("RETURN"), remap = false)
    public void fcWrite(Writes write, CallbackInfo ci) {
        write.bool(fcForceDisablePredictTarget);
        write.bool(fcPreferBuildingTarget);
        fcSortf.write(write);
    }

    public void fcFindTarget() {
        float range = range();

        Runnable findUnitTarget = () -> {
            fcSortf.beforeTargeting();

            target = Units.bestEnemy(team, x, y, range, e -> !e.dead() && (fcFilter.filters.size > 0 ? fcFilter.unitFilter.get(e) : turretBlock.unitFilter.get(e)) && (e.isGrounded() || turretBlock.targetAir) && (!e.isGrounded() || turretBlock.targetGround), fcSortf.unitSortfs.size > 0 ? fcSortf : turretBlock.unitSort);
        };

        Runnable findBuildingTarget = () -> {
            if (!turretBlock.targetGround) return;
            target = null;
            float cost = Float.POSITIVE_INFINITY;

            fcSortf.beforeTargeting();

            Vars.indexer.allBuildings(x, y, range, building -> {
                if (building.team == Team.derelict && !Vars.state.rules.coreCapture) return;
                if (!turretBlock.buildingFilter.get(building) || !building.block.targetable || building.team == team || !(fcFilter.filters.size > 0 ? fcFilter.buildingFilter.get(building) : turretBlock.buildingFilter.get(building))) return;

                if (fcSortf.cost(building) < cost) target = building;
            });

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

    protected void findTarget() {
        fcFindTarget();
    }
}