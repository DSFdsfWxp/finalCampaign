package finalCampaign.runtime;

import java.io.*;
import arc.*;
import arc.files.*;
import arc.util.*;
import finalCampaign.*;
import mindustry.*;

public class standAloneRuntime implements IRuntime {

    private Fi rootDir;
    private Fi gameJar;

    public standAloneRuntime() {
        try {
            if (OS.isAndroid) {
                Object applicationInfo = Core.app.getClass().getMethod("getApplicationInfo").invoke(Core.app);
                String apkPath = (String) applicationInfo.getClass().getDeclaredField("sourceDir").get(applicationInfo);
                gameJar = new Fi(apkPath);
                rootDir = Core.settings.getDataDirectory();
            } else {
                gameJar = new Fi(new File(Vars.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
                rootDir = gameJar.parent();
            }
        } catch(Exception e) {
            throw new RuntimeException("Should be ok.", e);
        }
    }

    @Override
    public String name() {
        return "StandAlone";
    }

    @Override
    public String getVersion() {
        return version.inPackage.getVersionFull("standAlone");
    }

    @Override
    public Fi getRootPath() {
        return rootDir;
    }

    @Override
    public Fi getDataPath() {
        return Core.settings.getDataDirectory();
    }

    @Override
    public Fi getGameJar() {
        return gameJar;
    }

    @Override
    public Fi getModJar() {
        return Vars.modDirectory.child("finalCampaign.jar");
    }

    @Override
    public void install(Fi mod) throws RuntimeException {
        throw new RuntimeException(bundle.get("installer.androidUpdateNotice"));
    }

    @Override
    public void startupInstall() {
        ZipFi gameJarZip = new ZipFi(gameJar);
        Fi modFile = Vars.modDirectory.child("finalCampaign.jar");
        Fi modAssetsFile = gameJarZip.child("fcStandAloneMod.jar");
        Fi modAssetsSha256 = gameJarZip.child("fcStandAloneMod.jar.sha256");

        if (!modFile.exists() || !setting.getAndCast("standAloneMod-sha256", "").equals(modAssetsSha256.readString())) {
            if (modFile.exists())
                modFile.delete();

            modAssetsFile.copyTo(modFile);
            // fix Android dex dynamic load secure exception
            if (OS.isAndroid)
                modFile.file().setReadOnly();
            
            setting.put("standAloneMod-sha256", modAssetsSha256.readString());
            Core.settings.manualSave();
        }

        gameJarZip.delete(); // close zip
    }

}
