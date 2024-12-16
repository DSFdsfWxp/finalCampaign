package finalCampaign.launch;

import java.io.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;

public class bothLauncherVersion {
    public static int majorD;
    public static int minorD;
    public static int debugD;

    public static int majorA;
    public static int minorA;
    public static int debugA;

    private static boolean loaded = false;

    public static void load(Reader reader) {
        if (loaded) return;

        ObjectMap<String, String> map = new ObjectMap<>();
        PropertiesUtils.load(map, reader);

        if (OS.isAndroid) {
            majorA = Integer.parseInt(map.get("launcher.android.major", "0"));
            minorA = Integer.parseInt(map.get("launcher.android.minor", "0"));
            debugA = Integer.parseInt(map.get("launcher.android.debug", "0"));
        } else {
            majorD = Integer.parseInt(map.get("launcher.desktop.major", "0"));
            minorD = Integer.parseInt(map.get("launcher.desktop.minor", "0"));
            debugD = Integer.parseInt(map.get("launcher.desktop.debug", "0"));
        }

        Streams.close(reader);
        loaded = true;
    }

    public static String toVersionString() {
        return OS.isAndroid ? String.format("%d.%d.%d", majorA, minorA, debugA) : String.format("%d.%d.%d", majorD, minorD, debugD);
    }

    public static String toVersionString(Reader reader) {
        ObjectMap<String, String> map = new ObjectMap<>();
        PropertiesUtils.load(map, reader);
        int[] thisVersion = new int[3];

        if (OS.isAndroid) {
            thisVersion[0] = Integer.parseInt(map.get("launcher.android.major", "0"));
            thisVersion[1] = Integer.parseInt(map.get("launcher.android.minor", "0"));
            thisVersion[2] = Integer.parseInt(map.get("launcher.android.debug", "0"));
        } else {
            thisVersion[0] = Integer.parseInt(map.get("launcher.desktop.major", "0"));
            thisVersion[1] = Integer.parseInt(map.get("launcher.desktop.minor", "0"));
            thisVersion[2] = Integer.parseInt(map.get("launcher.desktop.debug", "0"));
        }

        Streams.close(reader);
        return String.format("%d.%d.%d", thisVersion[0], thisVersion[1], thisVersion[2]);
    }

    public static String toVersionString(File file) {
        bothZipFi zip = new bothZipFi(new bothFi(file));
        String res = toVersionString(zip.child("version.properties").reader());
        zip.delete(); // close zip file
        return res;
    }
}
