package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.math.geom.*;
import arc.util.io.*;
import finalCampaign.patch.*;
import mindustry.gen.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

@Mixin(TurretBuild.class)
public abstract class fcTurretBuild implements ControlBlock, Posc, IFcTurretBuild {
    @Shadow(remap = false)
    public abstract boolean hasAmmo();
    @Shadow(remap = false)
    public Vec2 targetPos;

    private boolean fcForceDisablePredictTarget = false;

    public boolean fcForceDisablePredictTarget() {
        return fcForceDisablePredictTarget;
    }

    public void fcForceDisablePredictTarget(boolean v) {
        fcForceDisablePredictTarget = v;
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
    }

    @Inject(method = "write", at = @At("RETURN"), remap = false)
    public void fcWrite(Writes write) {
        write.bool(fcForceDisablePredictTarget);
    }
}