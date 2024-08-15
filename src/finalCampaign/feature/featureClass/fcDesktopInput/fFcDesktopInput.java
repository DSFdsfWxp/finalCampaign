package finalCampaign.feature.featureClass.fcDesktopInput;

import arc.struct.*;
import finalCampaign.feature.featureClass.binding.*;
import mindustry.*;

public class fFcDesktopInput {
    public static Seq<bindingHandle> bindingHandleLst;
    public static Seq<Runnable> drawTopHandleLst;
    private static boolean inited = false;

    public static boolean supported() {
        return !Vars.headless;
    }

    public static void init() {
        bindingHandleLst = new Seq<>();
        drawTopHandleLst = new Seq<>();
    }

    public static void load() {
        inited = true;
    }

    public static void addBindingHandle(bindingHandle handle) {
        if (!inited) return;
        if (!bindingHandleLst.contains(handle)) bindingHandleLst.add(handle);
    }

    public static void addDrawTopHandle(Runnable handle) {
        if (!inited) return;
        if (!drawTopHandleLst.contains(handle)) drawTopHandleLst.add(handle);
    }
}
