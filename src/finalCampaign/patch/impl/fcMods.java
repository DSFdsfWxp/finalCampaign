package finalCampaign.patch.impl;

import java.io.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.files.*;
import arc.util.*;
import finalCampaign.*;
import finalCampaign.launch.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;

@Mixin(Mods.class)
public abstract class fcMods {
    @Shadow(remap = false)
    public abstract @Nullable ModMeta findMeta(Fi file);
    @Shadow(remap = false)
    private boolean requiresReload;

    @Inject(method = "importMod", at = @At("HEAD"), remap = false, cancellable = true)
    public void importMod(Fi file, CallbackInfoReturnable<LoadedMod> ci) throws IOException {
        if (file.isDirectory()) throw new RuntimeException("Directory is not supported.");
        Fi zip = new ZipFi(file);
        if(zip.list().length == 1 && zip.list()[0].isDirectory()){
            zip = zip.list()[0];
        }
        ModMeta meta = findMeta(zip);
        if (meta == null) throw new RuntimeException("Failed to resolve mod meta.");
        if (meta.name.equals("final-campaign")) {
            bothVersionControl.install(file.absolutePath(), version.toVersionString(file));
            requiresReload = true;
            ci.setReturnValue(new LoadedMod(file, zip, null, null, meta));
        }
    }
}
