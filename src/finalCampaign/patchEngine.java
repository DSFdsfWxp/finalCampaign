package finalCampaign;

import arc.files.*;
import arc.util.Log;
import finalCampaign.util.*;
import finalCampaign.dialog.*;
import mindustry.Vars;
import mindustry.core.*;
import mindustry.ui.dialogs.*;

public class patchEngine {

    private static Fi classDir;

    public static void init() {
        classDir = finalCampaign.dataDir.child("class");
        if (!classDir.exists()) classDir.mkdirs();

        //cache.init();
    }

    public static void load() {
        BaseDialog dialog = null;
        Fi mindustryClassFile = classDir.child("mindustry.jar");

        if (Vars.android) {
            if (!mindustryClassFile.exists()) {
                // we only offer prebuild jar file to this version
                if (Version.build != 146 ||
                    Version.number != 7 ||
                    Version.revision != 0 ||
                    !Version.modifier.equals("release") ||
                    !Version.type.equals("official")) dialog = new versionCheckFail(mindustryClassFile);
            }
        }

        if (dialog == null) {
            try {
                //pool.init();
            } catch(Exception e) {
                Log.err(e);
                Vars.ui.showException(e);

                asyncTask.interrupt();
                return;
            }

            return;
        }

        dialog.show();
        asyncTask.interrupt();
    }

    
}
