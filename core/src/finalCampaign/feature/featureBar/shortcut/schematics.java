package finalCampaign.feature.featureBar.shortcut;

import finalCampaign.feature.featureBar.*;
import mindustry.*;
import mindustry.gen.*;

public class schematics {
    public static fFeatureBar.actionFeatureButton button;

    public static void register() {
        button = new fFeatureBar.actionFeatureButton(Icon.paste, "schematics", () -> {
            Vars.ui.schematics.show();
        });

        fFeatureBar.registerFetureButton(button);
    }
}
