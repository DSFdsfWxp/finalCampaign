package finalCampaign.feature.featureBar.shortcut;

import finalCampaign.feature.featureBar.*;
import mindustry.*;
import mindustry.gen.*;

public class techTree {

    public static fFeatureBar.actionFeatureButton button;

    public static void register() {
        button = new fFeatureBar.actionFeatureButton(Icon.tree, "techTree", () -> {
            Vars.ui.research.show();
        });
        button.setValid(() -> Vars.state.isCampaign());

        fFeatureBar.registerFetureButton(button);
    }
}
