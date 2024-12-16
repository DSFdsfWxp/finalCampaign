package finalCampaign.patch;

import arc.func.*;
import arc.struct.*;
import mindustry.gen.*;
import mindustry.ui.*;

public interface IFcBlock {
    public float fcAttractiveness();
    public float fcAntiAttractiveness();
    public float fcHiddenness();
    public OrderedMap<String, Func<Building, Bar>> fcBarMap();
}
