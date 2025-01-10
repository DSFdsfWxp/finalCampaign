package finalCampaign;

import arc.*;
import arc.util.*;
import arc.files.*;
import finalCampaign.feature.*;
import finalCampaign.graphics.*;
import finalCampaign.input.*;
import finalCampaign.map.*;
import finalCampaign.net.*;
import finalCampaign.runtime.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mindustry.*;
import mindustry.game.EventType.*;

public class finalCampaign extends Mod {
    public static LoadedMod thisLoadedMod;
    public static ZipFi thisModZip;
    public static Fi dataDir;
    public static Fi tmpDir;
    public static IRuntime runtime;

    public finalCampaign() {
        dataDir = Vars.dataDirectory.child("finalCampaign");
        tmpDir = dataDir.child("tmp");

        if (!dataDir.exists()) dataDir.mkdirs();
        if (!tmpDir.exists()) tmpDir.mkdirs();
    }

    @Override
    public void init() {
        thisLoadedMod = Vars.mods.getMod(finalCampaign.class);
        thisModZip = new ZipFi(thisLoadedMod.file);

        version.init();

        Log.info(" # finalCampaign [prototypePhase]");
        Log.info(" # " + version.inPackage.getVersionFull("mod"));

        if (!Vars.headless) {
            Events.on(ClientLoadEvent.class, e -> {
                modStartup();
            });
        } else {
            modStartup();
        }
    }

    private void modStartup() {
        bundle.init();
        featureLoader.init();
        if (!Vars.headless) atlas.init();

        bundle.load();

        if (runtime == null) {
            install();
            return;
        }

        fcNet.register();
        fcMap.init();
        if (!Vars.headless) fcInput.load();

        features.add();
        featureLoader.load();

        if (!Vars.headless) {
            shaders.load();
            atlas.load();
            icons.load();
            outlineIcons.load();
            Events.on(ClientLoadEvent.class, e -> outlineIcons.generate());
        }

        System.gc();
    }

    @Override
    public void loadContent(){
        //Log.info("Loading some example content.");
    }

    // stop threads, clean up here
    public static void safetyExit() {
        Core.app.exit();
    }

    private void install() {
        if (OS.isAndroid) {
            Vars.ui.showOkText(bundle.get("error"), bundle.get("installer.androidNotice"), () -> {});
        } else if (Vars.headless) {
            Log.info("[finalCampaign] " + bundle.get("load.install"));
            try {
                mixinRuntime runtime = new mixinRuntime();
                runtime.install(thisLoadedMod.file);
                Log.info(bundle.format("installer.finishHint", runtime.getRootPath().child("fcLaunch." + (OS.isWindows ? "bat" : "sh")).absolutePath()));
            } catch(Exception e) {
                Log.err("[finalCampaign] " + bundle.get("error") + ": " + bundle.get("installer.fail"));
            }
        } else {
            Core.app.post(() -> {
                Vars.ui.showConfirm(bundle.format("load.installConfirm", version.inPackage.getVersionFull("mod"), version.inPackage.getVersionFull("launcher")), () -> {
                    Vars.ui.loadAnd(bundle.get("load.install"), () -> {
                        try {
                            mixinRuntime runtime = new mixinRuntime();
                            runtime.install(thisLoadedMod.file);
                            Vars.ui.showOkText(bundle.get("info"), bundle.format((Vars.steam ? "installer.finishHintSteam" : "installer.finishHint"), runtime.getRootPath().child("fcLaunch." + (OS.isWindows ? "bat" : "sh")).absolutePath()), () -> {});
                        } catch(Exception e) {
                            Log.err(e);
                            Vars.ui.showOkText(bundle.get("error"), bundle.get("installer.fail"), finalCampaign::safetyExit);
                            Vars.ui.showException(e);
                        }
                    });
                });
            });
        }
    }

}
