package finalCampaign.feature.featureClass.spritePacker;

import finalCampaign.*;
import mindustry.*;
import mindustry.gen.*;

public class fSpritePacker {
    public static boolean supported() {
        return !Vars.headless && !Vars.mobile && version.inPackage.isDebugVersion("mod");
    }

    public static void init() {}

    public static void load() {
        Vars.ui.settings.addCategory("Sprite Packer", Icon.file, t -> {
            new spritePackerDialog(t);
        });
    }
}
