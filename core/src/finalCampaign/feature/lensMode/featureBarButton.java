package finalCampaign.feature.lensMode;

import finalCampaign.feature.featureBar.*;
import finalCampaign.graphics.*;
import mindustry.*;
import mindustry.input.*;

public class featureBarButton {

    public static fFeatureBar.selectFeatureButton button;

    public static void register() {
        button = new fFeatureBar.selectFeatureButton("lensMode", () -> fLensMode.enabled);
        button.setValid(() -> fLensMode.enabled);

        button.addSelection(fcIcon.defaultCamera, "defaultCamera", () -> switchMode(fLensMode.lensMode.defaultCamera));
        button.addSelection(fcIcon.followCamera, "defaultCamera", () -> switchMode(fLensMode.lensMode.followCamera));
        button.addSelection(fcIcon.freeCamera, "defaultCamera", () -> switchMode(fLensMode.lensMode.freeCamera));

        fFeatureBar.registerFetureButton(button);
    }

    private static boolean switchMode(fLensMode.lensMode mode) {
        if (mode == fLensMode.lensMode.defaultCamera && Vars.control.input instanceof MobileInput)
            return false;

        fLensMode.setMode(mode);
        return true;
    }
}
