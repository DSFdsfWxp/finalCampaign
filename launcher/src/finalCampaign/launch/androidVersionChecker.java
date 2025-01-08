package finalCampaign.launch;

import java.util.*;
import arc.struct.*;

public class androidVersionChecker {
    public static boolean modNeedUpdate() {
        fi currentVersion = files.instance.dataDirectory().child("mod.version");
        fi inPackVersion = files.instance.internalFile("fcLaunch/mod.version");
        if (!currentVersion.exists()) return true;

        String[] currentVers = currentVersion.readString().split("\\.");
        String[] inPackVers = inPackVersion.readString().split("\\.");

        if (Integer.parseInt(inPackVers[0]) > Integer.parseInt(currentVers[0])) return true;
        if (Integer.parseInt(inPackVers[1]) > Integer.parseInt(currentVers[1])) return true;

        ObjectMap<String, Integer> map = new ObjectMap<>();
        map.put("debug", 0);
        map.put("prerelease", 1);
        map.put("release",2);

        if (map.get(inPackVers[2], 0) > map.get(currentVers[2], 0)) return true;

        return false;
    }

    public static void registerCurrentModVersion(int major, int minor, String type) {
        fi currentVersion = files.instance.dataDirectory().child("mod.version");
        currentVersion.writeString(String.format("%d.%d.%s", major, minor, type));
    }

    public static boolean checkNeedUpdate(String name) {
        fi currentVersion = files.instance.dataDirectory().child(name + ".sha256");
        fi inPackVersion = files.instance.internalFile("fcLaunch/" + name + ".sha256");
        if (!currentVersion.exists()) return true;
        if (!Arrays.equals(currentVersion.readBytes(), inPackVersion.readBytes())) return true;

        return false;
    }

    public static void registerCurrentVersion(String name) {
        fi currentVersion = files.instance.dataDirectory().child(name + ".sha256");
        fi inPackVersion = files.instance.internalFile("fcLaunch/" + name + ".sha256");
        if (currentVersion.exists()) currentVersion.delete();
        inPackVersion.copyTo(currentVersion);
    }
}
