package finalCampaign;

import arc.struct.*;
import arc.func.*;
import arc.util.Log;
import finalCampaign.feature.*;

public class featureLoader {
    
    private static Seq<Class<?>> featureLst;
    private static Cons<Float> progressCons;

    public static void init() {
        featureLst = new Seq<>();
        progressCons = null;
    }

    public static void setProgressCons(Cons<Float> cons) {
        progressCons = cons;
    }

    public static void add(Class<?> featureClass) {
        if (!util.isValidFeature(featureClass)) 
            throw new RuntimeException("Not a valid feature: " + featureClass.getName());
        featureLst.add(featureClass);
    }

    public static void load() {
        float i = 0;
        for (Class<?> feature : featureLst) {
            String featureName = util.getFeatureName(feature);
            try {
                feature.getDeclaredMethod("init").invoke(feature);
                Log.debug("featureLoader: inited " + featureName);
            } catch(Exception e) {
                Log.err("featureLoader: failed to init " + featureName, e);
            }
            i ++;
            if (progressCons != null) progressCons.get(i / (featureLst.size * 2));
        }

        for (Class<?> feature : featureLst) {
            String featureName = util.getFeatureName(feature);
            try {
                feature.getDeclaredMethod("load").invoke(feature);
                Log.debug("featureLoader: loaded " + featureName);
            } catch(Exception e) {
                Log.err("featureLoader: failed to load " + featureName, e);
            }
            i ++;
            if (progressCons != null) progressCons.get(i / (featureLst.size * 2));
        }
    }
}
