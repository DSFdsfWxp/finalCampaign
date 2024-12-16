package finalCampaign;

import arc.*;
import arc.util.*;
import arc.files.*;
import finalCampaign.feature.*;
import finalCampaign.graphics.*;
import finalCampaign.input.*;
import finalCampaign.util.*;
import finalCampaign.launch.*;
import finalCampaign.map.*;
import finalCampaign.net.*;
import finalCampaign.runtime.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mindustry.*;
import mindustry.game.EventType.*;

public class finalCampaign extends Mod {
    public static LoadedMod thisMod;
    public static ZipFi thisModFi;
    public static Fi dataDir;
    public static IRuntime runtime;

    public finalCampaign() {
        dataDir = Vars.dataDirectory.child("finalCampaign");
        if (!dataDir.exists()) dataDir.mkdirs();

        if (!OS.isAndroid)
            checkLoad();
    }

    @Override
    public void init() {
        thisMod = Vars.mods.getMod(finalCampaign.class);
        thisModFi = new ZipFi(thisMod.file);

        version.init();

        Log.info(" # finalCampaign [prototypePhase]");
        Log.info(" # " + version.toVersionString());

        if (!Vars.headless) {
            if (installer.inInstalledGame()) {
                modStartup();
            } else {
                Events.on(ClientLoadEvent.class, e -> {
                    modStartup();
                });
            }
        } else {
            modStartup();
        }
    }

    private void modStartup() {
        bundle.init();
        featureLoader.init();
        if (!Vars.headless) atlas.init();

        bundle.load();

        if (!installer.inInstalledGame()) {
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

    private void checkLoad() {
        if (installer.inInstalledGame() &&
            !finalCampaign.class.getClassLoader().equals(shareMixinService.getClassLoader()))
        {
            String path = Vars.dataDirectory.parent().child("mods").absolutePath();
            if (Vars.launchIDFile.exists()) {
                Log.err("Duplicated FinalCampaign mod load. Please check you import. FinalCampaign mod should only be placed in folder \"@\".", path);
                Core.app.exit();
            }
        }
    }

    private void install() {
        if (Vars.headless) {
            Log.info("[finalCampaign] " + bundle.get("load.install"));
            try {
                installer.install();
            } catch(Exception e) {
                Log.err("[finalCampaign] " + bundle.get("error") + ": " + bundle.get("installer.fail"));
                safetyExit();
            }
        } else {
            Core.app.post(() -> {
                Vars.ui.showConfirm(bundle.format("load.installConfirm", version.toVersionString(), bothLauncherVersion.toVersionString()), () -> {
                    Vars.ui.loadAnd(bundle.get("load.install"), () -> {
                        try {
                            installer.install();
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
