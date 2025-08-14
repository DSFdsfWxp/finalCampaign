package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import finalCampaign.feature.buildTargeting.*;
import finalCampaign.feature.buildTargetingLimit.*;
import finalCampaign.map.fcMap;
import finalCampaign.net.*;
import finalCampaign.patch.*;
import finalCampaign.util.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

@Mixin(value = TurretBuild.class, remap = false)
public abstract class fcTurretBuild extends Building implements ControlBlock, IFcTurretBuild {
    @Shadow(remap = false)
    public abstract boolean hasAmmo();
    @Shadow(remap = false)
    public Vec2 targetPos;
    @Shadow(remap = false)
    public @Nullable Posc target;
    @Shadow(remap = false)
    public Seq<Turret.AmmoEntry> ammo;
    @Shadow(remap = false)
    public int totalAmmo;

    @Shadow(remap = false)
    public abstract float range();
    @Shadow(remap = false)
    protected abstract boolean canHeal();
    @Shadow(remap = false)
    public abstract BulletType peekAmmo();

    private Turret fcTurretBlock;
    private IFcTurret fcTurret;
    private boolean fcForceDisablePredictTarget = false;
    private boolean fcPreferBuildingTarget = false;
    private fcSortf fcSortf;
    private fcFilter fcFilter;
    private float[] fcTargetScore = new float[1];
    private float[] fcTmpTargetScore;

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
        fcTurretBlock = (Turret) block;
        fcTurret = (IFcTurret) block;

        Building res = super.create(block, team);

        fcSortf = new fcSortf((TurretBuild)(Object) this);
        fcFilter = new fcFilter((TurretBuild)(Object) this);

        return res;
    }
    
    @Override
    public void playerPlaced(Object config) {
        super.playerPlaced(config);
        if (fcTurret.fcSortfData() != null) {
            fcCall.setBuildingSortf(this, fcTurret.fcSortfData());
        }
        fcCall.setTurretPreferBuildingTarget(this, fcTurret.fcPreferBuildingTarget());
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
        if (fcMap.currentVersion < 1) return;
        fcForceDisablePredictTarget = read.bool();
        fcPreferBuildingTarget = read.bool();
        fcSortf.read(read);
        fcFilter.read(read);
    }

    @Inject(method = "write", at = @At("RETURN"), remap = false)
    public void fcWrite(Writes write, CallbackInfo ci) {
        if (fcMap.exportingPlainSave) return;
        write.bool(fcForceDisablePredictTarget);
        write.bool(fcPreferBuildingTarget);
        fcSortf.write(write);
        fcFilter.write(write);
    }

    public BulletType useAmmo() {
        if (cheating())
            return peekAmmo();
        Turret.AmmoEntry entry = (Turret.AmmoEntry)this.ammo.peek();
        if (entry.amount < Short.MAX_VALUE) entry.amount -= fcTurretBlock.ammoPerShot;
        if (entry.amount <= 0)
            this.ammo.pop();
        if (entry.amount < Short.MAX_VALUE) this.totalAmmo -= fcTurretBlock.ammoPerShot;
        this.totalAmmo = Math.max(this.totalAmmo, 0);
        return entry.type();
    }

    public void fcFindTarget() {
        float range = range();
        fcSortf.beforeTargeting();
        fcFilter.beforeTargeting();
        target = null;
        arrays.fillF(fcTargetScore, 0f);

        Runnable findUnitTarget = () -> {
            if(team == Team.derelict) return;
            fcTargetScore = arrays.ensureLengthF(fcTargetScore, fcSortf.unitSortfs.size + 1);

            Units.nearbyEnemies(team, x - range, y - range, range * 2f, range * 2f, unit -> {
                if (unit.dead() || !(fcFilter.filters.size > 0 ? fcFilter.unitFilter.get(unit) : fcTurretBlock.unitFilter.get(unit)) || (unit.isGrounded() && !fcTurretBlock.targetGround) || (!unit.isGrounded() && !fcTurretBlock.targetAir)) return;
                if(unit.team == Team.derelict || !unit.within(x, y, range + unit.hitSize / 2f) || !unit.targetable(team) || unit.inFogTo(team)) return;
    
                fcTmpTargetScore = fcSortf.score(unit, fcTmpTargetScore);
                for (int i=0; i<fcTmpTargetScore.length; i++) {
                    if (fcTargetScore[i] < fcTmpTargetScore[i] || target == null) {
                        target = unit;
                        System.arraycopy(fcTmpTargetScore, 0, fcTargetScore, 0, fcTmpTargetScore.length);
                    }
                    if (fcTargetScore[i] != fcTmpTargetScore[i]) break;
                }
            });
        };

        Runnable findUnitTargetRaw = () -> {
            target = Units.bestEnemy(team, x, y, range, e -> !e.dead() && (fcFilter.filters.size > 0 ? fcFilter.unitFilter.get(e) : fcTurretBlock.unitFilter.get(e)) && (e.isGrounded() || fcTurretBlock.targetAir) && (!e.isGrounded() || fcTurretBlock.targetGround), fcTurretBlock.unitSort);
        };

        Runnable findBuildingTarget = () -> {
            if (!fcTurretBlock.targetGround) return;
            fcTargetScore = arrays.ensureLengthF(fcTargetScore, fcSortf.buildSortfs.size + 1);
            
            Vars.indexer.allBuildings(x, y, range, building -> {
                if (building.team == Team.derelict && !Vars.state.rules.coreCapture) return;
                if (!building.block.targetable || building.team == team || !(fcFilter.filters.size > 0 ? fcFilter.buildingFilter.get(building) : fcTurretBlock.buildingFilter.get(building))) return;

                fcTmpTargetScore = fcSortf.score(building, fcTmpTargetScore);
                for (int i=0; i<fcTmpTargetScore.length; i++) {
                    if (fcTargetScore[i] < fcTmpTargetScore[i] || target == null) {
                        target = building;
                        System.arraycopy(fcTmpTargetScore, 0, fcTargetScore, 0, fcTmpTargetScore.length);
                    }
                    if (fcTargetScore[i] != fcTmpTargetScore[i]) break;
                }
            });

            if (target == null) target = canHeal() ? Units.findAllyTile(team, x, y, range, b -> b.damaged() && b != this) : null;
        };

        Runnable currentFindUnitTarget = fcSortf.unitSortfs.size > 0 ? findUnitTarget : findUnitTargetRaw;

        if (fcPreferBuildingTarget) {
            findBuildingTarget.run();
            if (target == null) currentFindUnitTarget.run();
        } else {
            currentFindUnitTarget.run();
            if (target == null) findBuildingTarget.run();
        }
    }

    protected void findTarget() {
        fcFindTarget();
    }
}