package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import finalCampaign.patch.*;
import mindustry.gen.*;
import mindustry.world.consumers.*;

@Mixin(ConsumePower.class)
public abstract class fcConsumePower extends Consume {
    @Shadow(remap = false)
    public float usage;
    @Shadow(remap = false)
    public float capacity;
    @Shadow(remap = false)
    public boolean buffered;

    @Inject(method = "requestedPower", remap = false, at = @At("RETURN"), cancellable = true)
    private void fcRequestedPower(Building entity, CallbackInfoReturnable<Float> ci) {
        if (entity instanceof IFcBuilding building) if (building.fcInfinityPower()) ci.setReturnValue(0f);
    }
}
