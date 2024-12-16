package finalCampaign.patch.impl;

import java.net.URI;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.backend.sdl.*;
import arc.backend.sdl.jni.*;
import finalCampaign.net.*;

@Mixin(SdlApplication.class)
public class fcSdlApplication {
    @Inject(method = "init", remap = false, at = @At("RETURN"))
    private void fcInit(CallbackInfo ci) {
        SDL.SDL_StopTextInput();
    }

    @Inject(method = "openURI", remap = false, at = @At("HEAD"), cancellable = true)
    private void fcOpenURI(String url, CallbackInfoReturnable<Boolean> ci) {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (Throwable e) {
            ci.setReturnValue(false);
            return;
        }

        if (uri.getScheme().equals("finalCampaign"))
            ci.setReturnValue(uriProcessor.process(uri));
    }
}
