package finalCampaign.feature.hudUI;

import mindustry.*;

public class fHudUI {

    public static hudFixedLayer fixedLayer;


    public static boolean supported() {
        return !Vars.headless;
    }

    public static void earlyInit() {
        fixedLayer = new hudFixedLayer();
    }

    public static void earlyLoad() {
        fixedLayer.init();
    }

    public static void lateLoad() {
        fixedLayer.setup(Vars.ui.hudGroup);
    }
}
