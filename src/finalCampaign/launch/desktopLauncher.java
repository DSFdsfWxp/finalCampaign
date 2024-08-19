package finalCampaign.launch;

import java.io.*;
import arc.util.*;

public class desktopLauncher extends shareLauncher {
    private static shareLauncher instance;
    private static boolean isServer;
    private static bothFi dataDir;

    public static void main(String[] arg) {
        instance = new desktopLauncher();
        shareMixinService.parseArg(arg);

        bothFiles.ExternalStoragePath = OS.userHome + File.separator;
        bothFiles.LocalStoragePath = new File("../../../").getAbsolutePath() + File.separator;
        
        bothFiles.instance = new bothFiles() {
            public bothFi internalFile(String path) {
                return new bothFi(path, bothFi.FileType.internal);
            }

            public bothFi dataDirectory() {
                return dataDir.child("finalCampaign");
            }
        };

        shareMixinService.thisJar = new bothFi(desktopLauncher.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        bothFi path = shareMixinService.thisJar.parent().parent().parent().parent();
        bothFi configFi = path.child("fcConfig.properties");

        checkFiExists(configFi, "FinalCampaign Mod Launcher Config File");

        bothConfigUtil.config config = null;
        try {
            config = bothConfigUtil.read(configFi.reader());
        } catch (Exception e) {
            Log.err(e);
            Log.err("[finalCampaign] Make sure the mod launcher config file is correct.");
            System.exit(1);
        }

        shareMixinService.gameJar = path.child(config.gameJarName);
        dataDir = new bothFi(config.dataDir);
        isServer = config.isServer;
        bothVersionControl.init(true);
        bothVersionControl.clean();
        shareMixinService.mod = bothVersionControl.currentMod();

        checkFiExists(shareMixinService.gameJar, "Mindustry Game Jar File");
        checkFiExists(dataDir, "Game's Data Directory");
        checkFiExists(shareMixinService.mod, "FinalCampaign Mod Jar File");

        bothFi modsFi = dataDir.child("finalCampaign").child("mods/");
        bothFi mod = modsFi.child("finalCampaign.jar");
        modsFi.mkdirs();
        if (!mod.exists()) mod.writeString("NOTICE: This file is a placeholder for finalCampaign mod. ");

        bothLauncherVersion.load((new bothZipFi(shareMixinService.mod)).child("version.properties").reader());
        shareCrashSender.setDefaultUncaughtExceptionHandler(new desktopCrashSender());

        instance.init();
        instance.startup();
    }

    private static void checkFiExists(bothFi fi, String name) {
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

    protected bothFi[] getJar() {
        return new bothFi[] {shareMixinService.gameJar, shareMixinService.mod};
    }

    protected void launch() throws Exception {
        Class<?> main = shareMixinService.getClassLoader().loadClass("finalCampaign.launch.sideDesktopMain");
        main.getDeclaredMethod("main", String.class, boolean.class, String[].class).invoke(null, dataDir.absolutePath(), isServer, (Object) shareMixinService.startupArgs);
    }
}
