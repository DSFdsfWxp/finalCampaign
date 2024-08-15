package finalCampaign.patch.impl;

import java.io.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import finalCampaign.feature.featureClass.buildTargeting.*;
import finalCampaign.feature.featureClass.buildTargetingLimit.*;
import finalCampaign.feature.featureClass.mapVersion.*;
import finalCampaign.patch.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
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

        Building res =  super.create(block, team);

        fcSortf = new fcSortf((TurretBuild)(Object) this);
        fcFilter = new fcFilter((TurretBuild)(Object) this);

        if (fcTurret.fcSortfData() != null) {
            Reads reads = new Reads(new DataInputStream(new ByteArrayInputStream(fcTurret.fcSortfData())));
            fcSortf.read(reads);
            if (!fcSortf.isValid()) fcSortf = new fcSortf((TurretBuild)(Object) this);
            reads.close();
        }
        fcPreferBuildingTarget = fcTurret.fcPreferBuildingTarget();

        return res;
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
        fcFilter.read(read);
    }

    @Inject(method = "write", at = @At("RETURN"), remap = false)
    public void fcWrite(Writes write, CallbackInfo ci) {
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

        Runnable findUnitTarget = () -> {
            fcSortf.beforeTargeting();

            target = Units.bestEnemy(team, x, y, range, e -> !e.dead() && (fcFilter.filters.size > 0 ? fcFilter.unitFilter.get(e) : fcTurretBlock.unitFilter.get(e)) && (e.isGrounded() || fcTurretBlock.targetAir) && (!e.isGrounded() || fcTurretBlock.targetGround), fcSortf.unitSortfs.size > 0 ? fcSortf : fcTurretBlock.unitSort);
        };

        Runnable findBuildingTarget = () -> {
            if (!fcTurretBlock.targetGround) return;
            target = null;
            float cost = Float.POSITIVE_INFINITY;

            fcSortf.beforeTargeting();

            Vars.indexer.allBuildings(x, y, range, building -> {
                if (building.team == Team.derelict && !Vars.state.rules.coreCapture) return;
                if (!fcTurretBlock.buildingFilter.get(building) || !building.block.targetable || building.team == team || !(fcFilter.filters.size > 0 ? fcFilter.buildingFilter.get(building) : fcTurretBlock.buildingFilter.get(building))) return;

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