package finalCampaign.feature.featureClass.control.freeVision;

import arc.*;
import finalCampaign.feature.featureClass.binding.*;
import finalCampaign.feature.featureClass.fcDesktopInput.*;
import finalCampaign.feature.featureClass.tuner.*;
import mindustry.*;

public class fFreeVision {
    
    private static boolean inited = false;
    private static boolean on;
    private static boolean enabled;
    private static config config;
    private static infoFragment lastFragment = null;

    public static class config {
        public boolean autoTargeting = true;
    }

    public static boolean supported() {
        return !Vars.headless;
    }

    public static void init() throws Exception {
        on = false;
        enabled = false;
        config = new config();
    }

    public static void load() throws Exception {
        fFcDesktopInput.addBindingHandle(new bindingHandle() {
            public void run() {
                checkOnOff();
            }
        });

        enabled = fTuner.add("freeVision", false, config, v -> enabled = v);

        inited = true;
    }

    public static boolean isOn() {
        return inited && on && enabled;
    }

    public static boolean autoTargetingEnabled() {
        return config.autoTargeting;
    }

    public static void checkOnOff() {
        if (Core.input.keyTap(binding.freeVision) && inited && enabled) {
            on = !on;

            if (lastFragment != null) lastFragment.remove();
            infoFragment info = new infoFragment();
            Vars.ui.hudGroup.addChild(info);
            lastFragment = info;
            info.added();
        }
    }
}
