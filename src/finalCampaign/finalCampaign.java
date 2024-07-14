package finalCampaign;

import arc.*;
import arc.util.*;
import arc.files.*;
import finalCampaign.dialog.*;
import finalCampaign.feature.*;
import finalCampaign.graphics.*;
import finalCampaign.launch.injector;
import finalCampaign.util.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mindustry.Vars;
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
            if (injector.injected() && !injector.inInjectedGame()) {
                thisMod = mods.getMod(finalCampaign.class);
                thisMod.meta.hidden = true;
                return;
            }

            loadDialog = new load();
            loadDialog.show();

            loadDialog.setTotalStep(7);
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
                asyncTask.subTask(atlas::init);

                asyncTask.defaultDelay(10f);
                asyncTask.subTask(0f, () -> loadDialog.nextStep("Loading bundle"));
                asyncTask.subTask(bundle::load);

                asyncTask.subTask(0f, () -> loadDialog.nextStep(bundle.get("load.checkingVersion")));
                if (!injector.injected()) {
                    if (injector.inInjectedGame()) {
                        asyncTask.subTask(() -> {
                            loadDialog.forceStop();
                            Log.err(new RuntimeException("A restart to original game jar is needed to update this mod."));
                            Vars.ui.showOkText(bundle.get("info"), bundle.get("injector.updateHint"), Core.app::exit);
                        });
                    } else {
                        asyncTask.subTask(0f, () -> loadDialog.setStepName(bundle.get("load.firstTimeInjection")));
                        asyncTask.subTask(() -> {
                            try {
                                injector.inject();
                            } catch(Exception e) {
                                loadDialog.forceStop();
                                Log.err(e);
                                Vars.ui.showOkText(bundle.get("error"), bundle.get("injector.fail"), Core.app::exit);
                                Vars.ui.showException(e);
                            }
                        });
                    }

                    return;
                }

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

                asyncTask.subTask(0f, () -> loadDialog.nextStep(bundle.get("load.loadingSprite")));
                asyncTask.subTask(atlas::load);

                asyncTask.subTask(0f, () -> loadDialog.nextStep(bundle.get("load.finish")));
            });

            loadTask.schedule();

        });
    }

    @Override
    public void loadContent(){
        //Log.info("Loading some example content.");
    }

    public static void safetyExit() {
        loadDialog.forceStop();
        Core.app.exit();
    }

}
