package finalCampaign.patch;

import arc.struct.*;
import mindustry.type.*;

public interface IFcDrillBuild {
    public void fcPreferItem(Item v);
    public Item fcDrillTarget();
    public ObjectIntMap<Item> fcScanOutput();
    public float fcCalcDrillSpeed(Item item, int amount);
}
