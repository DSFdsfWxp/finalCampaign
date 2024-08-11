package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import arc.func.*;
import arc.struct.*;
import finalCampaign.patch.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.*;

@Mixin(Block.class)
public abstract class fcBlock implements IFcBlock, IFcAttractiveEntityType {
    @Shadow(remap = false)
    protected OrderedMap<String, Func<Building, Bar>> barMap;

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

    public OrderedMap<String, Func<Building, Bar>> fcBarMap() {
        return barMap;
    }
}
