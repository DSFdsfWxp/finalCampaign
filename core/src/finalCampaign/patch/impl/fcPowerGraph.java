package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.struct.*;
import arc.util.*;
import finalCampaign.patch.*;
import mindustry.gen.*;
import mindustry.world.blocks.power.*;

@Mixin(PowerGraph.class)
public abstract class fcPowerGraph {

    @Shadow(remap = false)
    public Seq<Building> consumers;
    @Shadow(remap = false)
    public Seq<Building> batteries;

    @Shadow(remap = false)
    public abstract float getBatteryStored();
    
    @Inject(method = "update", at = @At("RETURN"), remap = false)
    public void fcUpdate(CallbackInfo ci) {
        for (Building b : consumers) if (((IFcBuilding) b).fcInfinityPower()) b.power.status = Float.POSITIVE_INFINITY;
    }

    @Inject(method = "chargeBatteries", at = @At("RETURN"), remap = false)
    private void fcChargeBatteries(float excess, CallbackInfoReturnable<Float> ci) {
        for (Building b : batteries) if (((IFcBuilding) b).fcInfinityPower()) b.power.status = Float.POSITIVE_INFINITY;
    }

    @Inject(method = "getBatteryStored", at = @At("RETURN"), remap = false, cancellable = true)
    private void fcGetBatteryStored(CallbackInfoReturnable<Float> ci) {
        if (Float.isNaN(ci.getReturnValueF())) ci.setReturnValue(0f);
    }

    @Inject(method = "useBatteries", at = @At("HEAD"), remap = false, cancellable = true)
    private void fcUseBatteries(float needed, CallbackInfoReturnable<Float> ci) {
        if (Float.isNaN(needed)) {
            ci.setReturnValue(0f);
            return;
        }

        if (getBatteryStored() == Float.POSITIVE_INFINITY) {
            if (needed == Float.POSITIVE_INFINITY || needed == Float.MAX_VALUE) {
                for (Building b : batteries) {
                    IFcBuilding f = (IFcBuilding) b;
                    if (f.fcInfinityPower()) {
                        if (b.health == Float.POSITIVE_INFINITY) b.health = b.maxHealth;
                        f.fcInfinityPower(false);
                        Time.run(120f, b::kill);
                        b.power.status = Float.NaN;
                    } else {
                        b.power.status = 0f;
                    }
                }
            }
            ci.setReturnValue(0f);
        }
    }
}
