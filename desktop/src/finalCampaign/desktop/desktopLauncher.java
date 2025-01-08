package finalCampaign.desktop;

import java.io.*;
import java.lang.reflect.*;

import arc.struct.Seq;
import arc.util.*;
import finalCampaign.launch.*;

public class desktopLauncher extends shareLauncher {
    private static shareLauncher instance;
    private static boolean isServer, isDebuging;
    private static fi rootDir, dataDir, gameJar, modJar;

    public static void main(String[] arg) throws Exception {
        instance = new desktopLauncher();

        files.ExternalStoragePath = OS.userHome + File.separator;
        files.LocalStoragePath = new File("").getAbsolutePath() + File.separator;
        
        files.instance = new files() {
            public fi internalFile(String path) {
                return new fi(path, fi.FileType.internal);
            }

            public fi dataDirectory() {
                return dataDir;
            }

            public fi rootDirectory() {
                return rootDir;
            }
        };

        fi thisJar = new fi(new File(desktopLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        rootDir = thisJar.parent().parent().parent().parent();
        fi configFi = rootDir.child("fcConfig.properties");

        checkFiExists(configFi, "FinalCampaign Mod Launcher Config File");

        desktopConfigUtil.config config = null;
        try {
            config = desktopConfigUtil.read(configFi.reader());
        } catch (Exception e) {
            Log.err(e);
            Log.err("[finalCampaign] Make sure the mod launcher config file is correct.");
            System.exit(1);
        }

        gameJar = rootDir.child(config.gameJarName);
        dataDir = new fi(config.dataDir);
        isServer = config.isServer;
        desktopVersionControl.init();
        modJar = desktopVersionControl.currentMod();

        checkFiExists(gameJar, "Mindustry Game Jar File");
        checkFiExists(dataDir, "Game's Data Directory");
        checkFiExists(modJar, "FinalCampaign Mod Jar File");

        shareCrashSender.setDefaultUncaughtExceptionHandler(new desktopCrashSender());

        Seq<String> argSeq = new Seq<>(arg);
        isDebuging = argSeq.contains("-fcDebug");

        instance.init(arg);
        instance.startup();
    }

    private static void checkFiExists(fi fi, String name) {
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

    protected fi[] getJar() {
        return new fi[] {gameJar, modJar};
    }

    protected void launch() throws Exception {
        shareClassLoader cl = shareMixinService.getClassLoader();
        Class<?> modClass = cl.loadClass("finalCampaign.finalCampaign");
        Class<?> modVersionClass = cl.loadClass("finalCampaign.version");
        Class<?> mixinRuntimeClass = cl.loadClass("finalCampaign.runtime.mixinRuntime");

        Field runtime = modClass.getDeclaredField("runtime");
        Field debuging = modVersionClass.getDeclaredField("isDebuging");
        Constructor<?> mixinRuntimeCtor = mixinRuntimeClass.getDeclaredConstructor(File.class, File.class);

        runtime.set(null, mixinRuntimeCtor.newInstance(gameJar.file(), dataDir.file()));
        debuging.set(null, isDebuging);
    }
}
