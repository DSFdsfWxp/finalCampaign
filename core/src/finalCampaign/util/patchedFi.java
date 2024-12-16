package finalCampaign.util;

import java.io.*;
import arc.files.*;
import arc.struct.*;

public class patchedFi extends Fi {
    private ObjectMap<String, Fi> patchLst = new ObjectMap<>();
    private final boolean readOnly;

    public patchedFi(Fi src) {
        this(src, false);
    }

    public patchedFi(Fi src, boolean readOnly) {
        super(src.file());
        this.readOnly = readOnly;
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

    @Override
    public boolean delete() {
        if (readOnly) return false;
        return super.delete();
    }

    @Override
    public OutputStream write(boolean append) {
        if (readOnly) return new ByteArrayOutputStream();
        return super.write(append);
    }
}
