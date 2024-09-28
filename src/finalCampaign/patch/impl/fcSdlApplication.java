package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.backend.sdl.*;
import arc.backend.sdl.jni.*;

@Mixin(SdlApplication.class)
public class fcSdlApplication {
    @Inject(method = "init", remap = false, at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        SDL.SDL_StopTextInput();
    }
}
