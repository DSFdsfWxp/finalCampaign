package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.files.*;
import mindustry.*;

@Mixin(Vars.class)
public abstract class fcVars {
    @Inject(method = "init", at = @At("RETURN"), remap = false)
    private static void fcInit(CallbackInfo ci) {
        Fi dataDir = Vars.dataDirectory.child("finalCampaign");

        Vars.saveDirectory = dataDir.child("saves/");
        Vars.customMapDirectory = dataDir.child("maps/");
        Vars.mapPreviewDirectory = dataDir.child("previews/");
        Vars.tmpDirectory = dataDir.child("tmp/");
        Vars.schematicDirectory = dataDir.child("schematics/");

        Vars.saveDirectory.mkdirs();
        Vars.customMapDirectory.mkdirs();
        Vars.mapPreviewDirectory.mkdirs();
        Vars.tmpDirectory.mkdirs();
        Vars.schematicDirectory.mkdirs();

        Vars.maps.reload();
    }
}
