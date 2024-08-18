package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.*;
import arc.files.*;
import arc.util.*;
import finalCampaign.launch.*;
import finalCampaign.util.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.io.*;

@Mixin(Vars.class)
public abstract class fcVars {
    private static boolean steam;
    private static String versionModifier;

    @Inject(method = "loadSettings", at = @At("HEAD"), remap = false)
    private static void fcLoadSettingsHead(CallbackInfo ci) {
        if (OS.isAndroid) return;
        Core.settings.setJson(JsonIO.json);
        Core.settings.setAppName(Vars.appName);

        Fi dataDir = Core.settings.getDataDirectory().child("finalCampaign");
        dataDir.mkdirs();

        patchedFi patchedModsFi = new patchedFi(new patchedFi(dataDir.child("mods"), true));
        patchedModsFi.mkdirs();
        patchedFi patchedDataFi = new patchedFi(dataDir);
        patchedDataFi.addPatchLst("mods", patchedModsFi);
        patchedModsFi.addPatchLst(shareMixinService.mod.nameWithoutExtension() + ".jar", new Fi(shareMixinService.mod.file()));

        Core.settings.setDataDirectory(patchedDataFi);

        steam = Vars.steam;
        versionModifier = Version.modifier;
        Vars.steam = false;
        Version.modifier = "";
    }

    @Inject(method = "loadSettings", at = @At("RETURN"), remap = false)
    private static void fcLoadSettingsReturn(CallbackInfo ci) {
        if (OS.isAndroid) return;
        Vars.steam = steam;
        Version.modifier = versionModifier;
    }
}
