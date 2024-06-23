package finalCampaign.feature.featureClass.crosshair;

import finalCampaign.feature.featureClass.tuner.*;
import mindustry.*;

public class fCrosshair {
    private static crosshairFragment fragment;

    public static void init() {
        fragment = new crosshairFragment(Vars.ui.hudGroup);
    }

    public static void load() {
        fTuner.add("centerCrosshair", false, custom -> {
            custom.checkSetting("point", false);
            custom.sliderSetting("movingOpacity", 1f, 1f, 0f, 0.001f);
            custom.sliderSetting("staticOpacity", 0.4f, 1f, 0f, 0.001f);
            custom.sliderSetting("scaleX", 1f, 4f, 0f, 0.1f);
            custom.sliderSetting("scaleY", 1f, 4f, 0f, 0.1f);
        });
        Vars.ui.hudGroup.addChild(fragment);
        fragment.added();
    }

    public static boolean isOn() {
        return fTuner.isOn("centerCrosshair");
    }

    public static float movingOpacity() {
        return fTuner.getCustomValue("centerCrosshair", "movingOpacity", Float.class);
    }

    public static float staticOpacity() {
        return fTuner.getCustomValue("centerCrosshair", "staticOpacity", Float.class);
    }

    public static float scaleX() {
        return fTuner.getCustomValue("centerCrosshair", "scaleX", Float.class);
    }

    public static float scaleY() {
        return fTuner.getCustomValue("centerCrosshair", "scaleY", Float.class);
    }

    public static boolean usingPoint() {
        return fTuner.getCustomValue("centerCrosshair", "point", Boolean.class);
    }
}
