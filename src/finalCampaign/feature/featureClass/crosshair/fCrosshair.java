package finalCampaign.feature.featureClass.crosshair;

import arc.graphics.*;
import finalCampaign.feature.featureClass.fcDesktopInput.*;
import finalCampaign.feature.featureClass.tuner.*;
import finalCampaign.feature.featureClass.tuner.fTuner.*;
import mindustry.*;

public class fCrosshair {
    private static boolean enabled;
    private static config config;
    private static crosshairFragment fragment;

    public static class config {
        public boolean point = false;
        public floatSlider movingOpacity = new floatSlider(1f, 1f, 0f, 0.001f);
        public floatSlider staticOpacity = new floatSlider(0.4f, 1f, 0f, 0.001f);
        public floatSlider scaleX = new floatSlider(1f, 4f, 0f, 0.1f);
        public floatSlider scaleY = new floatSlider(1f, 4f, 0f, 0.1f);
        public boolean invertColor = true;
        public Color color = Color.white.cpy();
    }

    public static boolean supported() {
        return !Vars.headless;
    }

    public static void init() {
        fragment = new crosshairFragment(Vars.ui.hudGroup);
        enabled = false;
        config = new config();
    }

    public static void load() {
        enabled = fTuner.add("centerCrosshair", false, config, v -> enabled = v);
        Vars.ui.hudGroup.addChild(fragment);
        fragment.added();
        fFcDesktopInput.addBindingHandle(fragment::checkMoving);
    }

    public static boolean isOn() {
        return enabled;
    }

    public static float movingOpacity() {
        return config.movingOpacity.value;
    }

    public static float staticOpacity() {
        return config.staticOpacity.value;
    }

    public static float scaleX() {
        return config.scaleX.value;
    }

    public static float scaleY() {
        return config.scaleY.value;
    }

    public static boolean usingPoint() {
        return config.point;
    }

    public static boolean isInvertColor() {
        return config.invertColor;
    }

    public static Color color() {
        return config.color;
    }
}
