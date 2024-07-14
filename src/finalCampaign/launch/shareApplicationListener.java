package finalCampaign.launch;

import java.lang.reflect.*;
import org.spongepowered.asm.launch.*;
import org.spongepowered.asm.mixin.*;
import arc.*;
import arc.files.*;
import arc.util.*;
import arc.util.Log.*;

public abstract class shareApplicationListener implements ApplicationListener {
    private ApplicationListener instance;

    public shareApplicationListener() {
        instance = this;
    }

    protected abstract void handleCrash(Throwable e, String msg);
    protected abstract shareClassLoader createClassLoader();
    protected abstract ApplicationListener createApplicationListener();
    protected abstract Fi[] getJar();
    protected abstract void beforeLaunch();

    @Override
    public void init() {
        try {
            shareLaunchRenderer.init();
        } catch(Exception e) {
            handleCrash(e, "Failed to init load renderer.");
        }
        shareLogger.setup();

        shareMixinPatcher.patch();
    }

    @Override
    public void update() {
        while (!shareLaunchRenderer.done()) {
            shareLaunchRenderer.draw();
            return;
        }

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

            createConfiguration.invoke(null, "fcMixin/config.json", MixinEnvironment.getCurrentEnvironment());

            classLoader = createClassLoader();
            classLoader.init();
            shareMixinService.setClassLoader(classLoader);
            Thread.currentThread().setContextClassLoader(classLoader);

        } catch(Exception e) {
            handleCrash(e, "Failed to boot mixin.");
        }

        Log.info("[finalCampaign] launching mindustry");
        try {
            beforeLaunch();

            String settingKey = "mod-final-campaign-enabled";
            if (!Core.settings.getBool(settingKey, true)) {
                Core.settings.put(settingKey, true);
                Core.settings.manualSave();
            }

            for (Fi jar : getJar()) classLoader.addJar(jar);

            ApplicationListener listener = createApplicationListener();
            Core.app.addListener(listener);
            listener.init();
            Core.app.removeListener(instance);
        } catch(Exception e) {
            handleCrash(e, "Failed to launch mindustry.");
        }

    }
}
