package finalCampaign.patch.impl;

import mindustry.core.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import finalCampaign.*;

@Mixin(UI.class)
public class fcUI {

    @Inject(method = "init", at = @At("HEAD"), remap = false)
    private void fcInit(CallbackInfo ci) {
        finalCampaign.beforeUiInit();
    }
}
