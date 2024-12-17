package finalCampaign.launch;

import java.lang.reflect.*;
import org.spongepowered.asm.launch.*;
import org.spongepowered.asm.mixin.*;
import arc.util.*;
import arc.util.Log.*;

public abstract class shareLauncher {
    protected abstract void handleCrash(Throwable e, String msg);
    protected abstract shareClassLoader createClassLoader();
    protected abstract bothFi[] getJar();
    protected abstract void launch() throws Exception;

    protected String configName = null;

    public void init() {
        shareLogger.setup();
        Log.info("[finalCampaign] pre-main bootstrap");
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

            String config = configName == null ? (OS.isAndroid ? "fcMixin/config.android.json" : "fcMixin/config.json") : "fcMixin/" + configName;
            createConfiguration.invoke(null, config, MixinEnvironment.getCurrentEnvironment());

            classLoader = createClassLoader();
            classLoader.init();
            shareMixinService.setClassLoader(classLoader);
            Thread.currentThread().setContextClassLoader(classLoader);

        } catch(Exception e) {
            handleCrash(e, "Failed to boot mixin.");
        }

        Log.info("[finalCampaign] launching mindustry");
        try {
            for (bothFi jar : getJar()) classLoader.addJar(jar);
            launch();
        } catch(Exception e) {
            handleCrash(e, "Failed to launch mindustry.");
        }

    }
}
