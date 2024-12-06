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
    private static boolean fcSteam;
    private static String fcVersionModifier;

    @Inject(method = "loadSettings", at = @At("HEAD"), remap = false)
    private static void fcLoadSettingsHead(CallbackInfo ci) {
        if (OS.isAndroid)
            return;

        Core.settings.setJson(JsonIO.json);
        Core.settings.setAppName(Vars.appName);

        Fi dataDir = new Fi(shareMixinService.dataDir.file()).child("finalCampaign/saves").child(Version.type);
        dataDir.mkdirs();

        patchedFi patchedModsFi = new patchedFi(new patchedFi(dataDir.child("mods"), true));
        patchedModsFi.mkdirs();
        patchedFi patchedDataFi = new patchedFi(dataDir);
        patchedDataFi.addPatchLst("mods", patchedModsFi);
        patchedModsFi.addPatchLst("finalCampaign.jar", new Fi(shareMixinService.mod.file()));

        Fi modPlaceholder = dataDir.child("mods/finalCampaign.jar");
        if (!modPlaceholder.exists())
            modPlaceholder.writeString("NOTICE: This file is a placeholder for finalCampaign mod. ");

        Core.settings.setDataDirectory(patchedDataFi);

        fcSteam = Vars.steam || (Version.modifier != null && Version.modifier.contains("steam"));
        fcVersionModifier = Version.modifier;

        Vars.steam = false;
        Version.modifier = "";
    }

    @Inject(method = "loadSettings", at = @At("RETURN"), remap = false)
    private static void fcLoadSettingsReturn(CallbackInfo ci) {
        if (OS.isAndroid)
            return;

        Vars.steam = fcSteam;
        Version.modifier = fcVersionModifier;
    }
}
