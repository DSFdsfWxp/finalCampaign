package finalCampaign;

import arc.util.Log;
import arc.util.Log.*;

public class version {
    
    public static final int major = 0;
    public static final int minor = 1;

    public static final boolean debug = true;
    /** only debug / prerelease / release accepted  */
    public static final String type = "debug";

    public static void init() {
        if (debug) {
            Log.level = LogLevel.debug;

            bundle.clearCache();
            Log.info("debug mode: cleared bundle cache.");
        }
        
    }

    public static String toVersionString() {
        return String.format("v%d.%d %s", major, minor, type) + (debug ? " [debug]" : "");
    }
    
}
