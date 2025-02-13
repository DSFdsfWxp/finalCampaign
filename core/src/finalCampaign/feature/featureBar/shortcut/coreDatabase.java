package finalCampaign.feature.featureBar.shortcut;

import finalCampaign.feature.featureBar.*;
import mindustry.*;
import mindustry.gen.*;

public class coreDatabase {

    public static fFeatureBar.actionFeatureButton button;

    public static void register() {
        button = new fFeatureBar.actionFeatureButton(Icon.book, "coreDatabase", () -> {
            Vars.ui.database.show();
        });

        fFeatureBar.registerFetureButton(button);
    }
}
