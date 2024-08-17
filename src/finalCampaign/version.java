package finalCampaign;

import arc.files.Fi;
import arc.struct.*;
import arc.util.*;
import arc.util.Log.*;
import arc.util.io.*;
import finalCampaign.launch.*;
import finalCampaign.util.*;

public class version {
    
    public static int major;
    public static int minor;

    public static boolean debug;
    /** only debug / preRelease / release accepted  */
    public static String type = "debug";

    public static void init() {
        Fi versionFile = finalCampaign.thisModFi.child("version.properties");

        bothLauncherVersion.load(versionFile.reader());

        ObjectMap<String, String> map = new ObjectMap<>();
        PropertiesUtils.load(map, versionFile.reader());
        major = Integer.parseInt(map.get("mod.major", "0"));
        minor = Integer.parseInt(map.get("mod.minor", "0"));
        type = map.get("mod.type", "debug");

        Class<?> service = reflect.findClass("finalCampaign.launch.shareMixinService", version.class.getClassLoader());
        debug = service == null ? false : Reflect.get(service, "debug");

        if (!type.equals("debug") && !type.equals("preRelease") && !type.equals("release"))
            throw new RuntimeException("Unacceptable version type: " + type);

        if (debug) Log.level = LogLevel.debug;
    }

    public static String toVersionString() {
        return String.format("v%d.%d %s", major, minor, type) + (debug ? " [debug]" : "");
    }
    
}
