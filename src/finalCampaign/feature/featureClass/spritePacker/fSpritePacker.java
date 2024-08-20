package finalCampaign.feature.featureClass.spritePacker;

import finalCampaign.*;
import mindustry.*;
import mindustry.gen.*;

public class fSpritePacker {
    public static boolean supported() {
        return !Vars.headless;
    }

    public static void init() {
        if (!version.isDebuging) return;
    }

    public static void load() {
        if (!version.isDebuging) return;
        
        Vars.ui.settings.addCategory("Sprite Packer", Icon.file, t -> {
            new spritePackerDialog(t);
        });
    }
}
