package finalCampaign.launch;

import java.io.*;
import arc.util.*;

public class bothVersionControl {
    private static boolean inited = false;
    private static boolean inInstalledGame;

    private static bothFi rootDirPath() {
        if (OS.isAndroid) {
            if (inInstalledGame) {
                return bothFiles.instance.dataDirectory().child("finalCampaignVersions");
            } else {
                return new bothFi("/");
            }
        } else {
            if (inInstalledGame) {
                bothFi classPath = new bothFi(shareMixinService.getClassPath());
                return classPath.parent().child("finalCampaign");
            } else {
                try {
                    bothFi classPath = new bothFi(new File(mindustry.Vars.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
                    return classPath.parent().child("finalCampaign");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static String currentModVersion() {
        try {
            return (new bothFi(rootDirPath().absolutePath() + "/mod/current")).readString();
        } catch(Exception e) {
            return "0.0-debug";
        }
    }

    public static String currentLauncherVersion() {
        try {
            return (new bothFi(rootDirPath().absolutePath() + "/launcher/current")).readString();
        } catch(Exception e) {
            return "0.0.0";
        }
    }

    public static bothFi currentMod() {
        if (!inInstalledGame) return null;
        return new bothFi(rootDirPath().absolutePath() + "/mod/" + currentModVersion() + "/mod.jar");
    }

    public static void clean() {
        if (!inInstalledGame) return;
        String modVer = currentModVersion();
        String launcherVer = currentLauncherVersion();
        bothFi rootDir = rootDirPath();
        bothFi modDir = rootDir.child("mod");
        bothFi launcherDir = rootDir.child("launcher");

        for (bothFi f : modDir.list()) {
            if (!f.isDirectory()) continue;
            if (!f.name().equals(modVer)) f.deleteDirectory();
        }

        if (!OS.isAndroid) {
            for (bothFi f : launcherDir.list()) {
                if (!f.isDirectory()) continue;
                if (!f.name().equals(launcherVer)) f.deleteDirectory();
            }
        }
    }

    public static void init(boolean inInstalledGame) {
        if (inited) return;
        bothVersionControl.inInstalledGame = inInstalledGame;
        bothFi root = rootDirPath();
        root.mkdirs();
        root.child("mod").mkdirs();
        root.child("launcher").mkdirs();
        inited = true;
    }

    public static void install(File file, String modVersion) {
        bothFi rootDir = rootDirPath();
        bothFi modFi = new bothFi(file);
        String launcherVersion = bothLauncherVersion.toVersionString(file);

        bothFi modDir = rootDir.child("mod").child(modVersion);
        modDir.mkdirs();
        modFi.copyTo(modDir.child("mod.jar"));
        rootDir.child("mod").child("current").writeString(modVersion);

        bothFi launcherDir = rootDir.child("launcher").child(launcherVersion);
        bothFi launcherJar = launcherDir.child("launcher.jar");
        launcherDir.mkdirs();
        if (!launcherJar.exists()) {
            if (OS.isAndroid) throw new RuntimeException("reinstallNeeded");
            bothZipFi zip = new bothZipFi(modFi);
            zip.child("class").child("preMain.desktop.jar").copyTo(launcherJar);
            rootDir.child("launcher").child("current").writeString(launcherVersion);
        }
    }
}
