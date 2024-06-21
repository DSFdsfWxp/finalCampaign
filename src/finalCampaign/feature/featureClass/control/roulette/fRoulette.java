package finalCampaign.feature.featureClass.control.roulette;

import arc.*;
import finalCampaign.feature.featureClass.binding.*;
import finalCampaign.feature.featureClass.fcDesktopInput.*;
import mindustry.*;

public class fRoulette {
    private static boolean inited = false;
    private static boolean on;

    public static void init() {
        if (Vars.mobile) return;

        on = false;
    }

    public static boolean isOn() {
        return inited && on;
    }

    public static void load() {
        if (Vars.mobile) return;

        fFcDesktopInput.addBindingHandle(new bindingHandle() {
            public void run() {
                if (Core.input.keyDown(binding.roulette)) {
                    on = true;
                }

                if (Core.input.keyRelease(binding.roulette)) {
                    on = false;
                }
            }
        });

        inited = true;
    }
}
