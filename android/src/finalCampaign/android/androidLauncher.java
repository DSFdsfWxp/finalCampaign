package finalCampaign.android;

import arc.*;
import finalCampaign.*;
import finalCampaign.runtime.*;

// NOT actually the launcher
// used to init sth.

public class androidLauncher {
    public static void launch() {
        finalCampaign.runtime = new standAloneRuntime();
        version.isDebuging = false;

        String settingKey = "mod-final-campaign-enabled";
        if (!Core.settings.getBool(settingKey, true)) {
            Core.settings.put(settingKey, true);
            Core.settings.saveValues();
        }
    }
}
