package finalCampaign;

import arc.*;
import arc.util.*;
import arc.files.*;
import finalCampaign.dialog.*;
import finalCampaign.feature.*;
import finalCampaign.graphics.*;
import finalCampaign.util.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mindustry.game.EventType.*;

import static mindustry.Vars.*;

public class finalCampaign extends Mod{

    public static LoadedMod thisMod;
    public static ZipFi thisModFi;
    public static Fi dataDir;
    private static load loadDialog;

    public finalCampaign(){
        Log.info(" # finalCampaign [prototypePhase]");
        Log.info(" # " + version.toVersionString());

        //listen for game load event
        Events.on(ClientLoadEvent.class, event -> {
            loadDialog = new load();
            loadDialog.show();

            loadDialog.setTotalStep(6);
            loadDialog.setStepName("Waiting for other mods to be loaded");

            thisMod = mods.getMod(finalCampaign.class);
            thisModFi = new ZipFi(thisMod.file);

            dataDir = dataDirectory.child("finalCampaign");
            if (!dataDir.exists()) dataDir.mkdirs();

            asyncTask loadTask = new asyncTask(() -> {
                int targetfps = Core.settings.getInt("fpscap", 120);
                targetfps = Math.min(targetfps, 25);

                if (Core.graphics.getFramesPerSecond() - targetfps <= -5 || Core.settings.getDataDirectory().child("launchid.dat").exists()) {
                    
                    asyncTask.reschedule(60f);
                    return;
                }

                asyncTask.subTask(() -> loadDialog.nextStep("Initializing"));
                asyncTask.defaultDelay(5f);
                asyncTask.subTask(patchEngine::init);
                asyncTask.subTask(bundle::init);
                asyncTask.subTask(featureLoader::init);
                asyncTask.subTask(version::init);

                asyncTask.defaultDelay(10f);
                asyncTask.subTask(0f, () -> loadDialog.nextStep("Loading bundle"));
                asyncTask.subTask(bundle::load);
                asyncTask.subTask(0f, () -> loadDialog.nextStep(bundle.get("load.checkingVersion")));
                asyncTask.subTask(patchEngine::load);

                asyncTask.subTask(0f, () -> loadDialog.nextStep(bundle.get("load.loadingFeature")));
                asyncTask.subTask(0f, () -> {
                    features.add();
                    featureLoader.setProgressCons((p) -> {
                        loadDialog.setStepProgress(p);
                    });
                });
                asyncTask.subTask(featureLoader::load);

                asyncTask.subTask(0f, () -> loadDialog.nextStep(bundle.get("load.compilingShader")));
                asyncTask.subTask(shaders::load);

                asyncTask.subTask(0f, () -> loadDialog.nextStep(bundle.get("load.finish")));
            });

            loadTask.schedule();

        });
    }

    @Override
    public void loadContent(){
        //Log.info("Loading some example content.");
    }

}
