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
    
    @Inject(method = "update", at = @At("RETURN"), remap = false)
    public void fcUpdate(CallbackInfo ci) {
        for (Building b : consumers) if (((IFcBuilding) b).fcInfinityPower()) b.power.status = Float.POSITIVE_INFINITY;
    }

    @Inject(method = "useBatteries", at = @At("RETURN"), remap = false)
    public void fcUseBatteries(float needed, CallbackInfoReturnable<Float> ci) {
        for (Building b : batteries) if (b.power != null) {
            IFcBuilding f = (IFcBuilding) b;
            if (Float.isNaN(b.power.status) && f.fcInfinityPower()) {
                f.fcInfinityPower(false);
                if (b.health == Float.POSITIVE_INFINITY) b.health = b.maxHealth;
                Time.run(120f, b::kill);
            }
        }
    }
}
