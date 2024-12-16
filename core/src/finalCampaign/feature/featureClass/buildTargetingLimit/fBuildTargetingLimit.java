package finalCampaign.feature.featureClass.buildTargetingLimit;

import finalCampaign.feature.featureClass.buildTargetingLimit.filters.*;

public class fBuildTargetingLimit {
    public static boolean supported() {
        return true;
    }
    
    public static void init() {}

    public static void load() {
        fcFilter.register("maxHpLessThanDamagePerShot", maxHpLessThanDamagePerShot::new);
        fcFilter.register("maxHpLessThanDamagePerSec", maxHpLessThanDamagePerSec::new);
        fcFilter.register("hpLessThanTripleDamagePerShot", hpLessThanTripleDamagePerShot::new);
        fcFilter.register("hpLessThanTripleDamagePerSec", hpLessThanTripleDamagePerSec::new);
        fcFilter.register("buildingOnly", buildingOnly::new);
        fcFilter.register("unitOnly", unitOnly::new);
        fcFilter.register("notTooLessTarget", notTooLessTarget::new);
    }
}
