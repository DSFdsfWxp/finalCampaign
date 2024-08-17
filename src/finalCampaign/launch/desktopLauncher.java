package finalCampaign.launch;

import java.io.*;
import arc.util.*;

public class desktopLauncher extends shareLauncher {
    private static shareLauncher instance;
    private static boolean isServer;
    private static shareFi dataDir;
    private static shareFi mindustryCore;

    public static void main(String[] arg) {
        instance = new desktopLauncher();

        shareMixinService.parseArg(arg);

        shareFiles.ExternalStoragePath = OS.userHome + File.separator;
        shareFiles.LocalStoragePath = new File("").getAbsolutePath() + File.separator;
        
        shareFiles.instance = new shareFiles() {
            public shareFi internalFile(String path) {
                return new shareFi(path, shareFi.FileType.internal);
            }

            public shareFi dataDirectory() {
                return dataDir.child("finalCampaign");
            }
        };

        shareMixinService.thisJar = new shareFi(desktopLauncher.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        shareFi path = shareMixinService.thisJar.parent();
        shareFi configFi = path.child("fcConfig.properties");

        checkFiExists(configFi, "FinalCampaign Mod Launcher Config File");

        bothConfigUtil.config config = null;
        try {
            config = bothConfigUtil.read(configFi.reader());
        } catch (Exception e) {
            Log.err(e);
            Log.err("[finalCampaign] Make sure the mod launcher config file is correct.");
            System.exit(1);
        }

        mindustryCore = path.child(config.gameJarName);
        dataDir = new shareFi(config.dataDir);
        isServer = config.isServer;
        
        shareMixinService.mod = dataDir.child("mods/").child(config.modName);
        shareFi modsFi = dataDir.child("finalCampaign").child("mods/");
        shareFi mod = modsFi.child(config.modName);

        checkFiExists(mindustryCore, "Mindustry Game Jar File");
        checkFiExists(dataDir, "Game's Data Directory");
        checkFiExists(shareMixinService.mod, "FinalCampaign Mod Jar File");

        bothLauncherVersion.load((new shareZipFi(shareMixinService.mod)).child("version.properties").reader());
        if (!config.version.equals(bothLauncherVersion.toDesktopVersionString())) {
            Log.err("[finalCampaign] An update is needed. Run patch again.");
            System.exit(1);
        }

        modsFi.mkdirs();
        if (!mod.exists()) mod.writeString("NOTICE: This file is a placeholder for finalCampaign mod. The one in ../../mods/ will be loaded. ");

        shareCrashSender.setDefaultUncaughtExceptionHandler(new desktopCrashSender());

        instance.init();
        instance.startup();
    }

    private static void checkFiExists(shareFi fi, String name) {
        if (!fi.exists()) {
            Log.err("[finalCampaign] " + name + " is NOT existed: " + fi.absolutePath());
            Log.err("[finalCampaign] Move it back or run patch again or correct the mod launcher config file.");
            System.exit(1);
        }
    }

    protected void handleCrash(Throwable error, String desc) {
        shareCrashSender.sender.log(error);
        Log.err(error);
        System.exit(1);
    }

    protected shareClassLoader createClassLoader() {
        return new desktopClassLoader();
    }

    protected shareFi[] getJar() {
        return new shareFi[] {mindustryCore, shareMixinService.mod};
    }

    protected void launch() throws Exception {
        Class<?> main = shareMixinService.getClassLoader().loadClass("finalCampaign.launch.sideDesktopMain");
        main.getDeclaredMethod("main", String.class, boolean.class, String[].class).invoke(null, dataDir.absolutePath(), isServer, (Object) shareMixinService.startupArgs);
    }
}
