package finalCampaign;

import arc.*;
import arc.util.*;
import arc.files.*;
import finalCampaign.feature.*;
import finalCampaign.graphics.*;
import finalCampaign.input.*;
import finalCampaign.launch.*;
import finalCampaign.net.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mindustry.*;
import mindustry.game.EventType.*;

public class finalCampaign extends Mod {
    public static LoadedMod thisMod;
    public static ZipFi thisModFi;
    public static Fi dataDir;

    public finalCampaign() {
        dataDir = Vars.dataDirectory.child("finalCampaign");
        if (!dataDir.exists()) dataDir.mkdirs();

        checkLoad();
    }

    public void checkLoad() {
        if (injector.inInjectedGame() &&
            !finalCampaign.class.getClassLoader().equals(shareMixinService.getClassLoader()))
        {
            String path = Vars.dataDirectory.parent().child("mods").absolutePath();
            if (Vars.launchIDFile.exists()) {
                Log.err("Duplicated FinalCampaign mod load. Please check you import. FinalCampaign mod should only be placed in folder \"@\".", path);
                Core.app.exit();
            } else {
                if (!Vars.headless && Vars.ui != null) {
                    Core.app.post(() -> {
                        thisMod = Vars.mods.getMod(finalCampaign.class);
                        thisModFi = new ZipFi(thisMod.file);
                        String thisFilePath = thisMod.file.absolutePath();

                        bundle.init();
                        bundle.load();

                        Vars.ui.showOkText(bundle.get("error"), bundle.format("checkLoad.wrongImportWay", path, thisFilePath), () -> {});
                    });
                }
            }
        }
    }

    @Override
    public void init() {
        thisMod = Vars.mods.getMod(finalCampaign.class);
        thisModFi = new ZipFi(thisMod.file);

        version.init();

        Log.info(" # finalCampaign [prototypePhase]");
        Log.info(" # " + version.toVersionString());

        if (!Vars.headless) {
            if (injector.injected() && injector.inInjectedGame()) {
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
        if (injector.injected() && !injector.inInjectedGame()) {
            thisMod = Vars.mods.getMod(finalCampaign.class);
            thisMod.meta.hidden = true;
            return;
        }

        bundle.init();
        featureLoader.init();
        if (!Vars.headless) atlas.init();

        bundle.load();

        if (!injector.inInjectedGame()) {
            if (Vars.headless) {
                Log.info("[finalCampaign] " + bundle.get("load.firstTimeInjection"));
                try {
                    injector.inject();
                } catch(Exception e) {
                    Log.err("[finalCampaign] " + bundle.get("error") + ": " + bundle.get("injector.fail"));
                    safetyExit();
                }
            } else {
                Core.app.post(() -> {
                    Vars.ui.loadAnd(bundle.get("load.firstTimeInjection"), () -> {
                        try {
                            injector.inject();
                        } catch(Exception e) {
                            Log.err(e);
                            Vars.ui.showOkText(bundle.get("error"), bundle.get("injector.fail"), finalCampaign::safetyExit);
                            Vars.ui.showException(e);
                        }
                    });
                });
            }

            return;
        }

        fcCall.register();
        fcNet.register();
        if (!Vars.headless) fcInput.load();

        features.add();
        featureLoader.load();

        if (!Vars.headless) shaders.load();
        if (!Vars.headless) atlas.load();

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

}
