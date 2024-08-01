package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.struct.*;
import finalCampaign.patch.*;
import mindustry.gen.*;
import mindustry.world.blocks.power.*;

@Mixin(PowerGraph.class)
public abstract class fcPowerGraph {

    @Shadow(remap = false)
    public Seq<Building> consumers;
    
    @Inject(method = "update", at = @At("RETURN"), remap = false)
    public void fcUpdate(CallbackInfo ci) {
        for (Building b : consumers) if (((IFcBuilding) b).fcInfinityPower()) b.power.status = Float.POSITIVE_INFINITY;
    }
}
