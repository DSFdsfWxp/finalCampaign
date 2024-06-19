package finalCampaign.feature.featureClass.control.freeVision;

import arc.*;
import finalCampaign.feature.featureClass.binding.*;
import mindustry.*;
import mindustry.input.*;

public class fFreeVision {
    
    private static boolean inited = false;
    private static boolean on;
    private static infoFragment lastFragment = null;

    public static void init() throws Exception {
        if (Vars.mobile) return;
        on = false;
    }

    public static void load() throws Exception {
        if (Vars.mobile) return;

        InputHandler handler = (InputHandler) new fcDesktopInput();
        Vars.control.setInput(handler);

        inited = true;
    }

    public static boolean isOn() {
        return inited && on;
    }

    public static void checkOnOff() {
        if (Core.input.keyTap(binding.freeVision) && inited) {
            on = !on;

            if (lastFragment != null) lastFragment.remove();
            infoFragment info = new infoFragment();
            Vars.ui.hudGroup.addChild(info);
            lastFragment = info;
            info.added();
        }
    }
}
