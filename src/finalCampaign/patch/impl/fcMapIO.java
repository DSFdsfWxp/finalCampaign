package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.graphics.*;
import mindustry.io.*;
import mindustry.maps.*;

@Mixin(MapIO.class)
public abstract class fcMapIO {
    @Inject(method = "generatePreview(Lmindustry/maps/Map;)Larc/graphics/Pixmap;", at = @At("HEAD"), remap = false)
    private static void fcGeneratePreview(Map map, CallbackInfoReturnable<Pixmap> ci) {
        finalCampaign.map.fcMap.currentVersion = 0;
    }
}
