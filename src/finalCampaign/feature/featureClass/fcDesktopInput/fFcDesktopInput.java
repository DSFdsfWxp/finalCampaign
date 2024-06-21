package finalCampaign.feature.featureClass.fcDesktopInput;

import arc.struct.*;
import finalCampaign.feature.featureClass.binding.bindingHandle;
import mindustry.Vars;
import mindustry.input.InputHandler;

public class fFcDesktopInput {
    protected static Seq<bindingHandle> handleLst;
    private static boolean inited = false;

    public static void init() {
        if (Vars.mobile) return;

        handleLst = new Seq<>();
    }

    public static void load() {
        if (Vars.mobile) return;

        InputHandler handler = (InputHandler) new fcDesktopInput();
        Vars.control.setInput(handler);

        inited = true;
    }

    public static void addBindingHandle(bindingHandle handle) {
        if (!inited) return;
        if (!handleLst.contains(handle)) handleLst.add(handle);
    }
}
