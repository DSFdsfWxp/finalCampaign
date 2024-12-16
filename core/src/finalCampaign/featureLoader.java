package finalCampaign;

import arc.struct.*;
import arc.func.*;
import arc.util.*;
import finalCampaign.feature.*;

public class featureLoader {
    
    private static Seq<Class<?>> featureLst;
    private static Cons<Float> progressCons;
    private static int progress;

    public static void init() {
        featureLst = new Seq<>();
        progressCons = null;
    }

    public static void setProgressCons(Cons<Float> cons) {
        progressCons = cons;
        progress = 0;
    }

    public static void add(Class<?> featureClass) {
        if (!util.isValidFeature(featureClass)) 
            throw new RuntimeException("Not a valid feature: " + featureClass.getName());
        boolean supported = Reflect.invoke(featureClass, "supported");
        if (supported) featureLst.add(featureClass);
    }

    private static void updateProgress() {
        progress ++;
        if (progressCons != null) progressCons.get(((float) progress) / (featureLst.size * 2));
    }

    public static void load() {
        for (Class<?> feature : featureLst) {
            String featureName = util.getFeatureName(feature);
            try {
                feature.getDeclaredMethod("init").invoke(null);
                Log.debug("featureLoader: inited " + featureName);
            } catch(Exception e) {
                Log.err("featureLoader: failed to init " + featureName, e);
            }
            updateProgress();
        }

        for (Class<?> feature : featureLst) {
            String featureName = util.getFeatureName(feature);
            try {
                feature.getDeclaredMethod("load").invoke(null);
                Log.debug("featureLoader: loaded " + featureName);
            } catch(Exception e) {
                Log.err("featureLoader: failed to load " + featureName, e);
            }
            updateProgress();
        }
    }
}
