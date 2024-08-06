package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import finalCampaign.patch.*;
import mindustry.world.*;

@Mixin(Block.class)
public class fcBlock implements IFcBlock, IFcAttractiveEntityType {
    public float fcAttractiveness = 0f;
    public float fcAntiAttractiveness = 0f;
    public float fcHiddenness = 0f;

    public float fcAttractiveness() {
        return fcAttractiveness;
    }

    public float fcAntiAttractiveness() {
        return fcAntiAttractiveness;
    }

    public float fcHiddenness() {
        return fcHiddenness;
    }
}
