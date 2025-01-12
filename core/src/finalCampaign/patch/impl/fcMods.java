package finalCampaign.patch.impl;

import java.io.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.files.*;
import arc.util.*;
import finalCampaign.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;

@Mixin(Mods.class)
public abstract class fcMods {
    @Shadow(remap = false)
    public abstract @Nullable ModMeta findMeta(Fi file);
    @Shadow(remap = false)
    private boolean requiresReload;

    @Inject(method = "importMod", at = @At("HEAD"), remap = false, cancellable = true)
    private void fcImportMod(Fi file, CallbackInfoReturnable<LoadedMod> ci) throws IOException {
        if (file.isDirectory()) throw new RuntimeException("Directory is not supported.");

        Fi tmpMod = finalCampaign.tmpDir.child("mod.jar");
        if (tmpMod.exists())
            tmpMod.delete();
        file.copyTo(tmpMod);

        Fi zip = new ZipFi(tmpMod);
        if(zip.list().length == 1 && zip.list()[0].isDirectory()){
            zip = zip.list()[0];
        }
        ModMeta meta = findMeta(zip);
        if (meta == null)
            throw new RuntimeException("Failed to resolve mod meta.");

        if (meta.name.equals("final-campaign")) {
            try {
                finalCampaign.runtime.install(tmpMod);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            requiresReload = true;
            ci.setReturnValue(new LoadedMod(file, zip, null, null, meta));
        }

        zip.delete(); // close zip
        tmpMod.delete(); // clear tmp
    }

    @Inject(method = "load", at = @At("HEAD"), remap = false)
    private void fcLoad(CallbackInfo ci) {
        try {
            finalCampaign.runtime.startupInstall();
        } catch(Exception e) {
            Log.err("Failed to excute startup install.", e);
        }
    }
}
