package finalCampaign.patch;

import finalCampaign.feature.buildTargeting.*;
import finalCampaign.feature.buildTargetingLimit.*;

public interface IFcTurretBuild {
    public boolean fcForceDisablePredictTarget();
    public void fcForceDisablePredictTarget(boolean v);
    
    public boolean fcPreferBuildingTarget();
    public void fcPreferBuildingTarget(boolean v);

    public fcSortf fcSortf();
    public fcFilter fcFilter();
    public void fcFindTarget();
}
