package finalCampaign.feature.barDetail;

import finalCampaign.*;
import finalCampaign.feature.tuner.*;
import finalCampaign.patch.*;
import mindustry.*;
import mindustry.world.*;

public class fBarDetail {
    private static boolean enabled;

    public static boolean supported() {
        return true;
    }

    public static void init() {
        enabled = false;
    }

    public static void load() {
        update(fTuner.add("barDetail", false, e -> {
            Vars.ui.loadAnd(bundle.get("reloading"), () -> {
                update(e);
            });
        }));
    }

    public static boolean isOn() {
        return enabled;
    }
    
    private static void update(boolean e) {
        if (enabled == e) return;
        enabled = e;

        for (Block b : Vars.content.blocks()) {
            IFcBlock fb = (IFcBlock) b;
            fb.fcBarMap().clear();
            b.setBars();
        }
    }
}
