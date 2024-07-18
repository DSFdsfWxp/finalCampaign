package finalCampaign.launch;

import arc.*;
import arc.backend.sdl.*;
import arc.util.*;
import mindustry.desktop.*;

public class sideDesktopMain {
    public static void main(String appName, String[] args) throws Exception {
        if (Core.settings == null) Core.settings = new Settings();
        if (Core.files == null) Core.files = new SdlFiles();

        Core.settings.setAppName(appName);
        Core.settings.loadValues();

        String settingKey = "mod-final-campaign-enabled";
        if (!Core.settings.getBool(settingKey, true)) {
            Log.info("[I] [finalCampaign] reEnable mod.");
            Core.settings.put(settingKey, true);
            Core.settings.saveValues();
        }

        DesktopLauncher.main(args);
    }
}
