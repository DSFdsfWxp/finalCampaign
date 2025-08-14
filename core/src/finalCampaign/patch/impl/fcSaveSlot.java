package finalCampaign.patch.impl;

import arc.files.*;
import finalCampaign.*;
import finalCampaign.map.fcMap;
import mindustry.*;
import mindustry.game.*;
import mindustry.io.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.io.*;

@Mixin(Saves.SaveSlot.class)
public class fcSaveSlot {
    @Shadow(remap = false)
    public Fi file;

    @Inject(method = "exportFile", at = @At("HEAD"), remap = false, cancellable = true)
    private void fcExportFile(Fi to, CallbackInfo ci) {
        Vars.ui.showCustomConfirm(
                bundle.get("dialog.exportPlainSave.title"),
                bundle.get("dialog.exportPlainSave.text"),
                bundle.get("yes"),
                bundle.get("no"),
                () -> Vars.ui.loadAnd(() -> {
                    try {
                        file.copyTo(to);
                    } catch (Throwable e) {
                        Vars.ui.showException(e);
                    }
                }),
                () -> {
                    fcMap.exportingPlainSave = true;
                    Vars.ui.loadAnd(() -> {
                        SaveIO.load(file);
                        try {
                            SaveIO.save(to);
                        } catch (Throwable e) {
                            Vars.ui.showException(e);
                        }
                        fcMap.exportingPlainSave = false;
                        Vars.logic.reset();
                    });
                }
        );
        ci.cancel();
    }
}
