package finalCampaign;

import arc.*;
import arc.util.*;
import arc.files.*;
import finalCampaign.feature.*;
import finalCampaign.graphics.*;
import finalCampaign.launch.*;
import finalCampaign.net.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mindustry.*;
import mindustry.game.EventType.*;

import static mindustry.Vars.*;

public class finalCampaign extends Mod{

    public static LoadedMod thisMod;
    public static ZipFi thisModFi;
    public static Fi dataDir;

    public finalCampaign(){
        Log.info(" # finalCampaign [prototypePhase]");
        Log.info(" # " + version.toVersionString());

        //listen for game load event
        Events.on(ClientLoadEvent.class, e -> {
            modStartup();
        });
        Events.on(ServerLoadEvent.class, e -> {
            modStartup();
        });
    }

    private void modStartup() {
        if (injector.injected() && !injector.inInjectedGame()) {
            thisMod = mods.getMod(finalCampaign.class);
            thisMod.meta.hidden = true;
            return;
        }

        thisMod = mods.getMod(finalCampaign.class);
        thisModFi = new ZipFi(thisMod.file);

        dataDir = dataDirectory.child("finalCampaign");
        if (!dataDir.exists()) dataDir.mkdirs();

        bundle.init();
        featureLoader.init();
        version.init();
        atlas.init();

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

        features.add();
        featureLoader.load();

        shaders.load();
        atlas.load();
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
