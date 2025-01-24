package finalCampaign.input;

import arc.*;

public class fcInput {
    public static void load() {
        fcInputHook.init();
        fcBindings.load();
        Core.input.addProcessor(new fcActionDetector());
    }
}
