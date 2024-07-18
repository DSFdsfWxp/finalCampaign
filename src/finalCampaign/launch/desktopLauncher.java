package finalCampaign.launch;

import java.io.*;
import arc.util.*;

public class desktopLauncher extends shareLauncher {
    private static String[] args;
    private static shareLauncher instance;
    private static String appName;
    private static shareFi dataDir;
    private static shareFi mindustryCore;

    public static void main(String[] arg) {
        args = arg;
        instance = new desktopLauncher();

        shareFiles.ExternalStoragePath = OS.userHome + File.separator;
        shareFiles.LocalStoragePath = new File("").getAbsolutePath() + File.separator;
        
        shareFiles.instance = new shareFiles() {
            public shareFi internalFile(String path) {
                return new shareFi(path, shareFi.FileType.internal);
            }

            public shareFi dataDirectory() {
                return dataDir;
            }
        };

        shareMixinService.thisJar = new shareFi(desktopLauncher.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        shareFi path = shareMixinService.thisJar.parent();
        shareFi configFi = path.child("fcConfig.bin");

        bothConfigUtil.config config = bothConfigUtil.read(configFi.read());
        mindustryCore = path.child(config.gameJarName);

        dataDir = new shareFi(config.dataDir);
        appName = config.appName;

        shareMixinService.mod = dataDir.child("mods/").child(config.modName);

        shareCrashSender.setDefaultUncaughtExceptionHandler(new desktopCrashSender());

        instance.init();
        instance.startup();
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
        main.getDeclaredMethod("main", String.class, String[].class).invoke(null, appName, (Object) args);
    }
}
