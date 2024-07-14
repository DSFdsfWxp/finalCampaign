package finalCampaign.launch;

import java.util.*;
import arc.*;
import arc.files.*;
import arc.struct.*;

public class androidVersionChecker {
    public static boolean modNeedUpdate() {
        Fi currentVersion = Core.settings.getDataDirectory().child("mod.version");
        Fi inPackVersion = Core.files.internal("fcLaunch/mod.version");
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
        Fi currentVersion = Core.settings.getDataDirectory().child("mod.version");
        currentVersion.writeString(String.format("%d.%d.%s", major, minor, type));
    }

    public static boolean gameNeedUpdate() {
        Fi currentVersion = Core.settings.getDataDirectory().child("game.sha256");
        Fi inPackVersion = Core.files.internal("fcLaunch/game.sha256");
        if (!currentVersion.exists()) return true;
        if (!Arrays.equals(currentVersion.readBytes(), inPackVersion.readBytes())) return true;

        return false;
    }

    public static void registerGameSha256() {
        Fi currentVersion = Core.settings.getDataDirectory().child("game.sha256");
        Fi inPackVersion = Core.files.internal("fcLaunch/game.sha256");
        if (currentVersion.exists()) currentVersion.delete();
        inPackVersion.copyTo(currentVersion);
    }

    public static boolean javaNeedUpdate() {
        Fi currentVersion = Core.settings.getDataDirectory().child("java.sha256");
        Fi inPackVersion = Core.files.internal("fcLaunch/java.sha256");
        if (!currentVersion.exists()) return true;
        if (!Arrays.equals(currentVersion.readBytes(), inPackVersion.readBytes())) return true;

        return false;
    }

    public static void registerJavaSha256() {
        Fi currentVersion = Core.settings.getDataDirectory().child("java.sha256");
        Fi inPackVersion = Core.files.internal("fcLaunch/java.sha256");
        if (currentVersion.exists()) currentVersion.delete();
        inPackVersion.copyTo(currentVersion);
    }
}
