package finalCampaign.util;

import arc.files.*;
import arc.struct.ObjectMap;

public class patchedFi extends Fi {
    private ObjectMap<String, Fi> patchLst = new ObjectMap<>();

    public patchedFi(Fi src) {
        super(src.file());
    }

    public void addPatchLst(String childName, Fi target) {
        Fi src = child(childName);
        if (patchLst.containsValue(src, false)) return;
        patchLst.put(src.absolutePath(), target);
    }

    @Override
    public Fi child(String name) {
        Fi src = super.child(name);
        Fi target = patchLst.get(src.absolutePath());
        return target == null ? src : target;
    }
}
