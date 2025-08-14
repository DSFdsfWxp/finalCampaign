package finalCampaign.feature;

import arc.struct.*;
import arc.util.*;
import finalCampaign.feature.about.*;
import finalCampaign.feature.auxDisplay.*;
import finalCampaign.feature.barDetail.*;
import finalCampaign.feature.blockShortcut.*;
import finalCampaign.feature.buildTargeting.*;
import finalCampaign.feature.buildTargetingLimit.*;
import finalCampaign.feature.crosshair.*;
import finalCampaign.feature.editMode.*;
import finalCampaign.feature.featureBar.*;
import finalCampaign.feature.hudUI.*;
import finalCampaign.feature.lensMode.*;
import finalCampaign.feature.pressingVisualHint.*;
import finalCampaign.feature.setMode.*;
import finalCampaign.feature.smartChoice.*;
import finalCampaign.feature.spritePacker.*;
import finalCampaign.feature.tuner.*;
import finalCampaign.feature.wiki.*;
import java.lang.reflect.*;


public class features {
    private static Seq<Class<?>> features = new Seq<>();

    public static void register(Class<?> feature) {
        try {
            boolean supported = Reflect.invoke(feature, "supported");
            if (supported) features.add(feature);
        } catch (Throwable e) {
            Log.err(e);
        }
    }

    public static void init() {
        register(fTuner.class);
        register(fCrosshair.class);
        register(fSpritePacker.class);
        register(fBlockShortcut.class);
        register(fSetMode.class);
        register(fWiki.class);
        register(fBuildTargeting.class);
        register(fBuildTargetingLimit.class);
        register(fBarDetail.class);
        register(fAbout.class);
        register(fPressingVisualHint.class);
        register(fEditMode.class);
        register(fLensMode.class);
        register(fFeatureBar.class);
        register(fHudUI.class);
        register(fSmartChoice.class);
        register(fAuxDisplay.class);
    }

    public enum featureLoadPhase {
        early,
        late
    }

    public static void load(featureLoadPhase phase) {
        Seq<Class<?>> featuresToLoad = new Seq<>(features);

        for (Class<?> feature : features) {
            try {
                Method initMethod = feature.getDeclaredMethod(phase.name() + "Init");
                Log.debug("[finalCampaign][features][@] initing feature: @", phase.name(), feature.getName());

                initMethod.invoke(null);
            } catch (Throwable e) {
                if (e instanceof NoSuchMethodException)
                    continue;
                Log.err("[finalCampaign][features][@] fail to init feature: @", phase.name(), feature.getName());
                Log.err(e);
                featuresToLoad.remove(feature);
            }
        }

        for (Class<?> feature : featuresToLoad) {
            try {
                Method loadMethod = feature.getDeclaredMethod(phase.name() + "Load");
                Log.debug("[finalCampaign][features][@] loading feature: @", phase.name(), feature.getName());

                loadMethod.invoke(null);
            } catch (Throwable e) {
                if (e instanceof NoSuchMethodException)
                    continue;
                Log.err("[finalCampaign][features][@] fail to load feature: @", phase.name(), feature.getName());
                Log.err(e);
            }
        }
    }
}
