package finalCampaign.feature.featureClass.spritePacker;

import mindustry.*;
import mindustry.gen.*;

public class fSpritePacker {
    public static boolean supported() {
        return !Vars.headless;
    }

    public static void init() {}

    public static void load() {
        Vars.ui.settings.addCategory("Sprite Packer", Icon.file, t -> {
            new spritePackerDialog(t);
        });
    }
}
