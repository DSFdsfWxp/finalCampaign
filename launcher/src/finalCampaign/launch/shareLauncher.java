package finalCampaign.launch;

import java.lang.reflect.*;
import org.spongepowered.asm.launch.*;
import org.spongepowered.asm.mixin.*;
import arc.struct.*;
import arc.util.*;
import arc.util.Log.*;

public abstract class shareLauncher {
    protected abstract void handleCrash(Throwable e, String msg);
    protected abstract shareClassLoader createClassLoader();
    protected abstract fi[] getJar();
    protected abstract void launch() throws Exception;

    public void init(String[] args) {
        shareLogger.setup();
        Log.info("[finalCampaign] launcher bootstrap");

        Seq<String> argsSeq = new Seq<>(args);
        if (argsSeq.contains("-fcBootstrapLog")) shareLogger.enableLog = true;
        if (argsSeq.contains("-fcMixinLog")) shareMixinLogger.enableLog = true;

    }

    public void startup() {
        shareClassLoader classLoader = null;

        try {
            Log.level = LogLevel.debug;

            Method gotoPhase = MixinEnvironment.class.getDeclaredMethod("gotoPhase", MixinEnvironment.Phase.class);
            Method createConfiguration = Mixins.class.getDeclaredMethod("createConfiguration", String.class, MixinEnvironment.class);

            gotoPhase.setAccessible(true);
            createConfiguration.setAccessible(true);

            MixinBootstrap.init();

            gotoPhase.invoke(null, MixinEnvironment.Phase.INIT);
            gotoPhase.invoke(null, MixinEnvironment.Phase.DEFAULT);

            createConfiguration.invoke(null, "finalCampaignMixinConfig.json", MixinEnvironment.getCurrentEnvironment());

            classLoader = createClassLoader();
            classLoader.init();
            shareMixinService.setClassLoader(classLoader);
            Thread.currentThread().setContextClassLoader(classLoader);

        } catch(Exception e) {
            handleCrash(e, "Failed to boot mixin.");
        }

        Log.info("[finalCampaign] launching...");
        try {
            for (fi jar : getJar()) classLoader.addJar(jar);
            launch();
        } catch(Exception e) {
            handleCrash(e, "Failed to launch.");
        }

    }
}
