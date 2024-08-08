package finalCampaign.feature.featureClass.buildTargeting;

import finalCampaign.feature.featureClass.buildTargeting.sortfs.*;

public class fBuildTargeting {

    public static void init() {}

    public static void load() {
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
