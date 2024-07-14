package finalCampaign.feature.featureClass.fcDesktopInput;

import arc.struct.*;
import finalCampaign.feature.featureClass.binding.*;

public class fFcDesktopInput {
    public static Seq<bindingHandle> handleLst;
    private static boolean inited = false;

    public static void init() {
        handleLst = new Seq<>();
    }

    public static void load() {
        inited = true;
    }

    public static void addBindingHandle(bindingHandle handle) {
        if (!inited) return;
        if (!handleLst.contains(handle)) handleLst.add(handle);
    }
}
