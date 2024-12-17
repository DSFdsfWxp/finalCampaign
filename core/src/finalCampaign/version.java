package finalCampaign;

import java.io.*;
import arc.files.*;
import arc.struct.*;
import arc.util.*;
import arc.util.Log.*;
import arc.util.io.*;

public class version {

    public static boolean isDebuging;
    public static version inPackage;

    private ObjectMap<String, String> versions, types;

    public version(Fi mod) {
        ZipFi modZip = new ZipFi(mod);
        Fi versionFile = modZip.child("version.properties");

        if (versionFile.exists()) {
            Reader reader = versionFile.reader();
            ObjectMap<String, String> map = new ObjectMap<>();
            PropertiesUtils.load(map, reader);
            Streams.close(reader);

            Seq<String> items = new Seq<>();
            for (String keys : map.keys()) {
                String name = keys.split(".")[0];
                if (!items.contains(name))
                    items.add(name);
            }

            for (String name : items) {
                versions.put(name, map.get(name + ".major", "0") + "." + 
                                   map.get(name + ".minor", "0") + "." + 
                                   map.get(name + ".debug", "0")
                            );
                
                types.put(name, map.get(name + ".type", "debug"));
            }
        }

        modZip.delete(); // close zip
    }

    public String getVersionFull(String name) {
        return versions.get(name) + "-" + types.get(name);
    }

    public String getVersionNumbers(String name) {
        return versions.get(name);
    }

    public String getVersionType(String name) {
        return types.get(name);
    }

    public boolean isDebugVersion(String name) {
        return types.get(name).equals("debug");
    }

    public boolean isReleaseVersion(String name) {
        return types.get(name).equals("release");
    }

    public static void init() {
        inPackage = new version(finalCampaign.thisLoadedMod.file);

        if (isDebuging) Log.level = LogLevel.debug;
    }
    
}
