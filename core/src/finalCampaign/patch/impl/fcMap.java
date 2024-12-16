package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.struct.*;
import mindustry.game.*;
import mindustry.maps.*;

@Mixin(Map.class)
public abstract class fcMap {
    @Shadow(remap = false)
    public StringMap tags;

    @Inject(method = "applyRules", at = @At("HEAD"), remap = false)
    private void fcApplyRules(Gamemode mode, CallbackInfoReturnable<Rules> ci) {
        tags.put("finalCampaign.appliedGamemode", Integer.toString(mode.ordinal()));
    }
}
