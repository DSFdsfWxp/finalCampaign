package finalCampaign;

import arc.*;
import arc.util.*;
import arc.files.*;
import finalCampaign.dialog.*;
import finalCampaign.feature.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mindustry.game.EventType.*;

import static mindustry.Vars.*;

public class finalCampaign extends Mod{

    public static LoadedMod thisMod;
    public static ZipFi thisModFi;
    public static Fi dataDir;
    public static volatile Thread gameThread;
    public volatile load loadDialog;

    public finalCampaign(){
        Log.info(" # finalCampaign [prototypePhase]");
        Log.info(" # " + version.toVersionString());

        gameThread = Thread.currentThread();

        //listen for game load event
        Events.on(ClientLoadEvent.class, event -> {
            loadDialog = new load();
            loadDialog.show();

            loadDialog.setTotalStep(5);
            loadDialog.setStepName("Waiting for other mods to be loaded");

            thisMod = mods.getMod(finalCampaign.class);
            thisModFi = new ZipFi(thisMod.file);

            dataDir = dataDirectory.child("finalCampaign");
            if (!dataDir.exists()) dataDir.mkdirs();

            Thread loadThread = new Thread(() -> {
                int targetfps = Core.settings.getInt("fpscap", 120);   
                targetfps = Math.min(targetfps, 25);

                while (Core.graphics.getFramesPerSecond() - targetfps <= -5 && gameThread.isAlive()) {
                    try {
                        Thread.sleep(1000);
                    } catch(Exception e) {}
                }

                if (!gameThread.isAlive()) return;

                loadDialog.nextStep("Initializing");
                patchEngine.init();
                bundle.init();
                featureLoader.init();
                version.init();
    
                loadDialog.nextStep("Loading bundle");
                bundle.load();
                loadDialog.nextStep(bundle.get("load.checkingVersion"));
                if (!patchEngine.load()) return;
    
                loadDialog.nextStep(bundle.get("load.loadingFeature"));
                features.add();
                featureLoader.setProgressCons((p) -> {
                    loadDialog.setStepProgress(p);
                });
                featureLoader.load();

                loadDialog.nextStep(bundle.get("load.finish"));
                //loadDialog.hide();
            });
            
            Time.run(180f, () -> {
                loadThread.start();
            });

        });
    }

    @Override
    public void loadContent(){
        //Log.info("Loading some example content.");
    }

}
