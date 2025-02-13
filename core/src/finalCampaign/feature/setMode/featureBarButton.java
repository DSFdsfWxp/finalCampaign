package finalCampaign.feature.setMode;

import finalCampaign.feature.featureBar.*;
import mindustry.gen.*;

public class featureBarButton {

    public static fFeatureBar.togglableFeatureButton button;

    public static void register() {
        button = new fFeatureBar.togglableFeatureButton(Icon.settings, "setMode", () -> {
            fSetMode.toggle();
            return false;
        });
        button.setValid(fSetMode::isEnabled);

        fFeatureBar.registerFetureButton(button);
    }
}
