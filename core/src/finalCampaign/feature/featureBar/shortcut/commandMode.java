package finalCampaign.feature.featureBar.shortcut;

import finalCampaign.feature.featureBar.*;
import mindustry.*;
import mindustry.gen.*;

public class commandMode {

    public static fFeatureBar.togglableFeatureButton button;

    public static void register() {
        button = new fFeatureBar.togglableFeatureButton(Icon.units, "commandMode", () -> {
            Vars.control.input.commandMode = !Vars.control.input.commandMode;
            return false;
        });

        fFeatureBar.registerFetureButton(button);
    }
}
