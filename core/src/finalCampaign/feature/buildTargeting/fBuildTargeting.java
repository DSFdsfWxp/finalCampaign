package finalCampaign.feature.buildTargeting;

import finalCampaign.feature.buildTargeting.sortfs.*;

public class fBuildTargeting {

    public static boolean supported() {
        return true;
    }

    public static void lateInit() {
        buildTargetingPreset.load();
    }

    public static void lateLoad() {
        fcSortf.register("highestMaxHp", highestMaxHp::new);
        fcSortf.register("highestHp", highestHp::new);
        fcSortf.register("lowHp", lowHp::new);
        fcSortf.register("closestToCore", closestToCore::new);
        fcSortf.register("closest", closest::new);
        fcSortf.register("farest", farest::new);
        fcSortf.register("uneffected", uneffected::new);
        fcSortf.register("effected", effected::new);
        fcSortf.register("mostEnemyDirection", mostEnemyDirection::new);
        fcSortf.register("categoryOfBuilding", categoryOfBuilding::new);
    }
}
