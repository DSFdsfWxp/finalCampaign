package finalCampaign.patch.impl;

import arc.files.*;
import finalCampaign.*;
import finalCampaign.map.fcMap;
import mindustry.*;
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
        fcMap.currentVersion = 0;
    }

    @Inject(method = "writeMap", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private static void fcWriteMap(Fi file, Map map, CallbackInfo ci) {
        if (fcMap.savingMap)
            return;

        Runnable writeMap = () -> {
            Vars.ui.loadAnd(() -> {
                try {
                    SaveIO.write(file, map.tags);
                } catch (Throwable e) {
                    Vars.ui.showException(e);
                }
                fcMap.exportingPlainSave = false;
            });
        };

        Vars.ui.showCustomConfirm(
                bundle.get("dialog.exportPlainSave.title"),
                bundle.get("dialog.exportPlainSave.text"),
                bundle.get("yes"),
                bundle.get("no"),
                writeMap,
                () -> {
                    fcMap.exportingPlainSave = true;
                    writeMap.run();
                }
        );

        ci.cancel();
    }
}
