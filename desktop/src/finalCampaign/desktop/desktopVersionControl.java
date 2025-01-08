package finalCampaign.desktop;

import finalCampaign.launch.*;

public class desktopVersionControl {
    private static boolean inited = false;

    private static fi rootDirPath() {
        return files.instance.rootDirectory().child("finalCampaign");
    }

    public static String currentModVersion() {
        try {
            return (new fi(rootDirPath().absolutePath() + "/mod/current")).readString();
        } catch(Exception e) {
            return "0.0-debug";
        }
    }

    public static String currentLauncherVersion() {
        try {
            return (new fi(rootDirPath().absolutePath() + "/launcher/current")).readString();
        } catch(Exception e) {
            return "0.0.0";
        }
    }

    public static fi currentMod() {
        return new fi(rootDirPath().absolutePath() + "/mod/" + currentModVersion() + "/mod.jar");
    }

    public static void init() {
        if (inited) return;
        fi root = rootDirPath();
        root.mkdirs();
        root.child("mod").mkdirs();
        root.child("launcher").mkdirs();
        inited = true;
    }
}
