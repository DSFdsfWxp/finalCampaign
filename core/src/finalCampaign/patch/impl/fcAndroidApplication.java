package finalCampaign.patch.impl;

import java.net.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.backend.android.*;
import finalCampaign.net.*;

@Mixin(AndroidApplication.class)
public abstract class fcAndroidApplication {

    @Inject(method = "openURI", at = @At("HEAD"), cancellable = true, remap = false)
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
