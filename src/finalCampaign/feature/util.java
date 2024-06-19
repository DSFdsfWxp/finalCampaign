package finalCampaign.feature;

import arc.struct.*;

public class util {

    public static boolean isValidFeature(Class<?> featureClass) {
        String className = featureClass.getName();
        
        if (!className.startsWith("finalCampaign.feature.featureClass.")) return false;
        return true;
    }

    public static String getFeatureName(Class<?> featureClass) {
        String className = featureClass.getName();
        if (!isValidFeature(featureClass)) throw new RuntimeException("Not a valid feature: " + className);
        
        Seq<String> splited = new Seq<>(className.substring(35).split("\\."));
        splited.pop();

        return String.join(".", splited);
    }
}
