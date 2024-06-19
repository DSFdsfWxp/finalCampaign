package finalCampaign;

import arc.util.Log;
import arc.util.Log.*;
import finalCampaign.patch.*;

public class version {
    
    public static final int major = 0;
    public static final int minor = 1;

    public static final boolean debug = true;
    /** only debug / prerelease / release accepted  */
    public static final String type = "debug";

    public static void init() {
        if (debug) {
            Log.level = LogLevel.debug;

            cache.clear();
            Log.info("debug mode: cleared patch cache.");

            bundle.clearCache();
            Log.info("debug mode: cleared bundle cache.");
        }
        
    }

    public static String toVersionString() {
        return "v" + Integer.toString(major) + "." + Integer.toString(minor) + " " + type + (debug ? "[debug]" : "");
    }
    
}
