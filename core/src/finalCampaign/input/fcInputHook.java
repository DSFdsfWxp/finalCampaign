package finalCampaign.input;

import arc.*;
import arc.KeyBinds.*;
import arc.input.*;
import finalCampaign.patch.*;

public class fcInputHook {
    private static IFcKeyboardDevice hooker;

    public static void init() {
        hooker = (IFcKeyboardDevice) Core.input.getKeyboard();
    }

    public static void installHook(inputHookPoint point, IBooleanInputHook hook) {
        hooker.fcInstallHook(point, hook);
    }

    public static void installAxisHook(IFloatInputHook hook) {
        hooker.fcInstallAxisHook(hook);
    }

    public static boolean realIsPressed(KeyBind bind) {
        KeyCode code = Core.keybinds.get(bind).key;
        return code != null && realIsPressed(code);
    }

    public static boolean realIsPressed(KeyCode code) {
        return hooker.fcRealIsPressed(code);
    }

    public static boolean realIsTapped(KeyBind bind) {
        KeyCode code = Core.keybinds.get(bind).key;
        return code != null && realIsTapped(code);
    }

    public static boolean realIsTapped(KeyCode code) {
        return hooker.fcRealIsTapped(code);
    }

    public static boolean realIsReleased(KeyBind bind) {
        KeyCode code = Core.keybinds.get(bind).key;
        return code != null && realIsReleased(code);
    }

    public static boolean realIsReleased(KeyCode code) {
        return hooker.fcRealIsReleased(code);
    }

    public static float getRealAxis(KeyBind bind) {
        KeyCode code = Core.keybinds.get(bind).key;
        return code != null ? getRealAxis(code) : 0f;
    }

    public static float getRealAxis(KeyCode code) {
        return hooker.fcGetRealAxis(code);
    }

    public static enum inputHookPoint {
        pressed,
        tapped,
        released
    }

    public static interface IBooleanInputHook {
        public boolean handle(KeyCode code, boolean value);
    }

    public static interface IFloatInputHook {
        public float handle(KeyCode code, float value);
    }
}
