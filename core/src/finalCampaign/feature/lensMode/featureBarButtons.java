package finalCampaign.feature.lensMode;

import finalCampaign.feature.featureBar.*;
import finalCampaign.graphics.*;
import mindustry.*;
import mindustry.input.*;

public class featureBarButtons {

    public static fFeatureBar.selectFeatureButton modeButton;
    public static fFeatureBar.togglableFeatureButton autoTargetingButton;
    public static fFeatureBar.togglableFeatureButton roamingBuildButton;

    public static void register() {
        {
            modeButton = new fFeatureBar.selectFeatureButton("lensMode", () -> fLensMode.enabled);
            modeButton.setValid(() -> fLensMode.enabled);

            modeButton.addSelection(fcIcon.defaultCamera, "defaultCamera", () -> switchMode(fLensMode.lensMode.defaultCamera));
            modeButton.addSelection(fcIcon.followCamera, "followCamera", () -> switchMode(fLensMode.lensMode.followCamera));
            modeButton.addSelection(fcIcon.freeCamera, "freeCamera", () -> switchMode(fLensMode.lensMode.freeCamera));

            modeButton.setCurrentSelection(fLensMode.mode.name());
            fFeatureBar.registerFetureButton(modeButton);
        }

        {
            autoTargetingButton = new fFeatureBar.togglableFeatureButton(fcIcon.target, "lensMode.autoTargeting", () -> {
                fLensMode.setAutoTargeting(!fLensMode.autoTargeting);
                return true;
            });
            autoTargetingButton.setValid(() -> fLensMode.enabled);
            autoTargetingButton.setChecked(fLensMode.autoTargeting);
            fFeatureBar.registerFetureButton(autoTargetingButton);
        }

        {
            roamingBuildButton = new fFeatureBar.togglableFeatureButton(fcIcon.roamingBuild, "lensMode.roamingBuild", () -> {
                fLensMode.setRoamingBuild(!fLensMode.roamingBuild);
                return true;
            });
            roamingBuildButton.setValid(() -> fLensMode.enabled && fLensMode.mode != fLensMode.lensMode.defaultCamera);
            roamingBuildButton.setChecked(fLensMode.roamingBuild);
            fFeatureBar.registerFetureButton(roamingBuildButton);
        }
    }

    private static boolean switchMode(fLensMode.lensMode mode) {
        if (mode == fLensMode.lensMode.defaultCamera && Vars.control.input instanceof MobileInput)
            return false;

        fLensMode.setMode(mode);
        return true;
    }
}
