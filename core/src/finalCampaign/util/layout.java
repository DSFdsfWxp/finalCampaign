package finalCampaign.util;

import arc.*;
import mindustry.*;

public class layout {
    public static float getSceneWidth() {
        if (Core.settings.getBool("landscape", false) && Vars.mobile) 
            return Math.max(Core.scene.getHeight(), Core.scene.getWidth());
        return Core.scene.getWidth();
    }

    public static float getSceneHeight() {
        if (Core.settings.getBool("landscape", false) && Vars.mobile) 
            return Math.min(Core.scene.getHeight(), Core.scene.getWidth());
        return Core.scene.getHeight();
    }
}
