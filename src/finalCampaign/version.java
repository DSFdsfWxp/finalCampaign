package finalCampaign;

import java.io.*;
import arc.files.*;
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
        Reader reader = versionFile.reader();
        PropertiesUtils.load(map, reader);
        Streams.close(reader);
        major = Integer.parseInt(map.get("mod.major", "0"));
        minor = Integer.parseInt(map.get("mod.minor", "0"));
        type = map.get("mod.type", "debug");
        bundle.bundleVersion = map.get("bundle", "0");

        Class<?> service = reflect.findClass("finalCampaign.launch.shareMixinService", version.class.getClassLoader());
        debug = service == null ? false : Reflect.get(service, "debug");

        if (!type.equals("debug") && !type.equals("preRelease") && !type.equals("release"))
            throw new RuntimeException("Unacceptable version type: " + type);

        if (debug) Log.level = LogLevel.debug;
    }

    public static String toVersionString() {
        return String.format("%d.%d-%s", major, minor, type);
    }

    public static String toVersionString(Reader reader) {
        ObjectMap<String, String> map = new ObjectMap<>();
        PropertiesUtils.load(map, reader);
        int major = Integer.parseInt(map.get("mod.major", "0"));
        int minor = Integer.parseInt(map.get("mod.minor", "0"));
        String type = map.get("mod.type", "debug");
        Streams.close(reader);
        return String.format("%d.%d-%s", major, minor, type);
    }

    public static String toVersionString(Fi mod) {
        ZipFi zip = new ZipFi(mod);
        String str = toVersionString(zip.child("version.properties").reader());
        zip.delete(); // close zip file
        return str;
    }
    
}
