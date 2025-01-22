package finalCampaign.feature;

import arc.struct.*;
import arc.util.*;
import finalCampaign.feature.about.*;
import finalCampaign.feature.barDetail.*;
import finalCampaign.feature.blockShortcut.*;
import finalCampaign.feature.buildTargeting.*;
import finalCampaign.feature.buildTargetingLimit.*;
import finalCampaign.feature.crosshair.*;
import finalCampaign.feature.editMode.*;
import finalCampaign.feature.freeVision.*;
import finalCampaign.feature.pressingVisualHint.*;
import finalCampaign.feature.roulette.*;
import finalCampaign.feature.setMode.*;
import finalCampaign.feature.shortcut.*;
import finalCampaign.feature.spritePacker.*;
import finalCampaign.feature.tuner.*;
import finalCampaign.feature.wiki.*;

public class features {
    private static Seq<Class<?>> features = new Seq<>();

    public static void register(Class<?> feature) {
        boolean supported = Reflect.invoke(feature, "supported");
        if (supported) features.add(feature);
    }

    public static void init() {
        register(fFreeVision.class);
        register(fTuner.class);
        register(fCrosshair.class);
        register(fSpritePacker.class);
        register(fBlockShortcut.class);
        register(fRoulette.class);
        register(fShortcut.class);
        register(fSetMode.class);
        register(fWiki.class);
        register(fBuildTargeting.class);
        register(fBuildTargetingLimit.class);
        register(fBarDetail.class);
        register(fAbout.class);
        register(fPressingVisualHint.class);
        register(fEditMode.class);
    }

    public static void load() {
        Seq<Class<?>> featuresToLoad = new Seq<>();

        for (Class<?> feature : features) {
            Log.debug("[finalCampaign][features] initing feature: " + feature.getName());
            try {
                Reflect.invoke(feature, "init");
                featuresToLoad.add(feature);
            } catch (Throwable e) {
                Log.err("[finalCampaign][features] fail to init feature: " + feature.getName());
                Log.err(e);
            }
        }

        for (Class<?> feature : featuresToLoad) {
            Log.debug("[finalCampaign][features] loading feature: " + feature.getName());
            try {
                Reflect.invoke(feature, "load");
            } catch (Throwable e) {
                Log.err("[finalCampaign][features] fail to load feature: " + feature.getName());
                Log.err(e);
            }
        }
    }
}
