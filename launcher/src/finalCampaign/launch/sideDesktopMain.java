package finalCampaign.launch;

import arc.*;
import arc.backend.sdl.*;
import arc.files.*;
import arc.mock.*;
import arc.util.*;
import mindustry.desktop.*;
import mindustry.server.*;

public class sideDesktopMain {
    public static void main(String dataDir, boolean isServer, String[] args) throws Exception {
        if (isServer) {
            serverInit();
        } else {
            desktopInit();
        }

        Fi dataDirFi = (new Fi(dataDir)).child("finalCampaign");
        Core.settings.setDataDirectory(dataDirFi);
        Core.settings.loadValues();

        String settingKey = "mod-final-campaign-enabled";
        if (!Core.settings.getBool(settingKey, true)) {
            Log.info("[I] [finalCampaign] reEnable mod.");
            Core.settings.put(settingKey, true);
            Core.settings.saveValues();
        }

        if (isServer) {
            serverMain(args);
        } else {
            desktopMain(args);
        }
    }

    private static void desktopInit() {
        if (Core.settings == null) Core.settings = new Settings();
        if (Core.files == null) Core.files = new SdlFiles();
    }

    private static void serverInit() {
        if (Core.settings == null) Core.settings = new Settings();
        if (Core.files == null) Core.files = new MockFiles();
    }

    private static void desktopMain(String[] args) {
        DesktopLauncher.main(args);
    }

    private static void serverMain(String[] args) {
        ServerLauncher.main(args);
    }
}
