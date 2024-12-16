package finalCampaign.launch;

import android.app.*;
import android.content.*;
import android.os.*;
import arc.*;
import arc.files.*;
import arc.util.*;
import mindustry.android.*;

public class sideAndroidMain {
    public static void main(String dataDir, Activity mainActivity, Bundle bundle) throws Exception {
        if (Core.settings == null) Core.settings = new Settings();
        
        Core.settings.setDataDirectory(new Fi(dataDir));
        Core.settings.loadValues();

        String settingKey = "mod-final-campaign-enabled";
        if (!Core.settings.getBool(settingKey, true)) {
            Log.info("[I] [finalCampaign] reEnable mod.");
            Core.settings.put(settingKey, true);
            Core.settings.saveValues();
        }

        Intent rawIntent = mainActivity.getIntent();
        Intent intent = new Intent(mainActivity, AndroidLauncher.class);
        intent.setAction("finalCampaignMod.LAUNCH");
        intent.setData(rawIntent.getData());
        //mainActivity.startActivity(intent);
        Activity main = new AndroidLauncher();
        main.setIntent(intent);
        main.onCreate(bundle, null);
    }
}
