package finalCampaign.runtime;

import java.io.*;
import arc.*;
import arc.util.*;
import arc.util.io.*;
import arc.files.*;
import arc.func.*;
import arc.struct.*;
import finalCampaign.*;
import mindustry.*;

public class mixinRuntime implements IRuntime {

    private Fi rootDir, dataDir;
    private Fi gameJar, modJar;
    private Fi modVersion, launcherVersion;

    public mixinRuntime() {
        this(null, null);
    }

    public mixinRuntime(File gameJar, File dataDir) {
        if (gameJar == null) {
            try {
                gameJar = new File(Vars.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            } catch (Exception e) {
                throw new RuntimeException("Should be ok.", e);
            }
            this.modJar = finalCampaign.thisLoadedMod.file;
        }

        this.rootDir = new Fi(gameJar.getParentFile());
        this.dataDir = dataDir == null ? Core.settings.getDataDirectory() : new Fi(dataDir);
        this.gameJar = new Fi(gameJar);
        modVersion = this.rootDir.child("finalCampaign/mod/current");
        launcherVersion = this.rootDir.child("finalCampaign/launcher/current");

        if (modJar == null && modVersion.exists())
            modJar = modVersion.parent().child(modVersion.readString()).child("mod.jar");

        clear();
    }

    @Override
    public String name() {
        return "Mixin";
    }

    @Override
    public Fi getRootPath() {
        return rootDir;
    }

    @Override
    public Fi getDataPath() {
        return dataDir;
    }

    @Override
    public Fi getGameJar() {
        return gameJar;
    }

    @Override
    public Fi getModJar() {
        return modJar;
    }

    @Override
    public String getVersion() {
        return launcherVersion.exists() ? launcherVersion.readString() : "0.0.0-debug";
    }

    @Override
    public void install(Fi mod) throws Exception {
        // version controller
        modVersion.parent().mkdirs();
        launcherVersion.parent().mkdirs();

        version modVersion = new version(mod);
        ZipFi modZip = new ZipFi(mod);
        String originalLauncherVersion = this.launcherVersion.exists() ? this.launcherVersion.readString() : "0.0.0-debug";

        Cons3<String, String, byte[]> runInstall = (type, ver, data) -> {
            Fi target = rootDir.child("finalCampaign").child(type).child(ver).child(type + ".jar");
            target.parent().mkdirs();
            target.writeBytes(data);
            target.parent().parent().child("current").writeString(ver);
        };

        if (!this.modVersion.exists() || !this.modVersion.readString().equals(modVersion.getVersionFull("mod")))
            runInstall.get("mod", modVersion.getVersionFull("mod"), mod.readBytes());

        if (!this.launcherVersion.exists() || !this.launcherVersion.readString().equals(modVersion.getVersionFull("launcher")))
            runInstall.get("launcher", modVersion.getVersionFull("launcher"), modZip.child("fcLaunch").child("launcher.jar").readBytes());

        // launcher
        if (!isBootedFromLauncher()) {
            // fc config
            Fi configFile = rootDir.child("fcConfig.properties");
            ObjectMap<String, String> configMap = new ObjectMap<>();

            configMap.put("gameJarName", gameJar.name());
            configMap.put("dataDir", isBootedFromLauncher() ? Core.settings.getDataDirectory().parent().parent().parent().absolutePath() : Core.settings.getDataDirectory().absolutePath());
            configMap.put("isServer", Vars.headless ? "true" : "false");

            PropertiesUtils.store(configMap, configFile.writer(false), "FinalCampaign Mod Launcher Configuration File");

            // launch script
            Fi scriptFile = (Vars.steam ? rootDir.parent() : rootDir).child("fcLaunch." + (OS.isWindows ? "bat" : "sh"));
            String script = modZip.child("fcLaunch").child("script").child((Vars.steam ? "steam" : "normal") + "." + (OS.isWindows ? "win.bat" : "unix.sh")).readString();
            scriptFile.writeString(script);

            // steam config
            if (Vars.steam) {
                Fi steamConfig = rootDir.parent().child("Mindustry.json");
                Fi steamConfigOriginal = rootDir.parent().child("Mindustry.json.original");
                Fi steamConfigFc = rootDir.parent().child("Mindustry.json.fc");

                if (!steamConfigOriginal.exists())
                    steamConfig.copyTo(steamConfigOriginal);

                String steamConfigJson = steamConfig.readString();
                String originalLauncherPath = "jre/finalCampaign/launcher/" + originalLauncherVersion + "/launcher.jar";
                String nowLauncherPath = "jre/finalCampaign/launcher/" + modVersion.getVersionFull("launcher") + "/launcher.jar";

                steamConfigJson = steamConfigJson.replace("mindustry.desktop.DesktopLauncher", "finalCampaign.desktop.desktopLauncher");
                steamConfigJson = steamConfigJson.replace("jre/desktop.jar", nowLauncherPath);
                steamConfigJson = steamConfigJson.replace(originalLauncherPath, nowLauncherPath);

                steamConfigFc.writeString(steamConfigJson);
            }
        }

        // clean up
        modZip.delete(); // close zip
    }

    @Override
    public void startupInstall() throws Exception {
        Fi modPlaceholder = Vars.modDirectory.child("finalCampaign.jar");
        if (!modPlaceholder.exists())
            modPlaceholder.writeString("NOTICE: This file is a placeholder for finalCampaign mod. ");
    }

    private void clear() {
        Cons2<Fi, String> runClear = (f, ver) -> {
            for (Fi c : f.list()) {
                if (c.isDirectory() && !c.name().equals(ver))
                    c.deleteDirectory();
            }
        };

        if (modVersion.exists())
            runClear.get(modVersion.parent(), modVersion.readString());

        if (launcherVersion.exists())
            runClear.get(launcherVersion.parent(), launcherVersion.readString());
    }

    private boolean isBootedFromLauncher() {
        try {
            Class.forName("finalCampaign.launch.shareLauncher", true, mixinRuntime.class.getClassLoader());
            return true;
        } catch(Exception e) {
            return false;
        }
    }
    
}
