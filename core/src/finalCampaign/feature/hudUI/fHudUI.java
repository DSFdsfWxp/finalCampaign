package finalCampaign.feature.hudUI;

import mindustry.*;

public class fHudUI {

    public static hudFixedLayer fixedLayer;
    public static hudWindowLayer windowLayer;


    public static boolean supported() {
        return !Vars.headless;
    }

    public static void earlyInit() {
        fixedLayer = new hudFixedLayer();
        windowLayer = new hudWindowLayer();
    }

    public static void earlyLoad() {
        fixedLayer.init();
    }

    public static void lateLoad() {
        fixedLayer.setup(Vars.ui.hudGroup);
        windowLayer.setup(Vars.ui.hudGroup);
    }
}
