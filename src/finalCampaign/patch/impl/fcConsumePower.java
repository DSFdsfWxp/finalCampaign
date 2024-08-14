package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
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

    public float requestedPower(Building entity){
        if (entity instanceof IFcBuilding building) if (building.fcInfinityPower()) return 0f;
        return buffered ?
            (1f - entity.power.status) * capacity :
            usage * (entity.shouldConsume() ? 1f : 0f);
    }
}
