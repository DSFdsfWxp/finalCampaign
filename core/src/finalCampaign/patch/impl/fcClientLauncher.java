package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.util.*;
import mindustry.*;

@Mixin(ClientLauncher.class)
public abstract class fcClientLauncher {

    @Inject(method = "setup", at = @At("HEAD"), remap = false)
    private void fcSetup(CallbackInfo ci) {
        if (OS.isAndroid) {
            try {
                Class<?> androidLauncherClass = Class.forName("finalCampaign.android.androidLauncher");
                Reflect.invoke(androidLauncherClass, "launch");
            } catch(Throwable e) {
                throw new RuntimeException("Should be ok.", e);
            }
        }
    }
}
