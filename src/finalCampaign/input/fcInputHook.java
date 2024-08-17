package finalCampaign.input;

import arc.*;
import arc.input.*;
import arc.struct.*;
import arc.util.*;

public class fcInputHook {
    private static Seq<IBooleanInputHook> pressedHooks;
    private static Seq<IBooleanInputHook> tappedHooks;
    private static Seq<IBooleanInputHook> releasedHooks;
    private static Seq<IFloatInputHook> axisHooks;
    private static fcInputHooker hooker;

    public static void init() {
        pressedHooks = new Seq<>();
        tappedHooks = new Seq<>();
        releasedHooks = new Seq<>();
        axisHooks = new Seq<>();
        hooker = new fcInputHooker();
        Reflect.set(Core.input, "keyboard", hooker);
    }

    public static void installHook(inputHookPoint point, IBooleanInputHook hook) {
        switch (point) {
            case pressed: {
                if (!pressedHooks.contains(hook)) pressedHooks.add(hook);
                break;
            }
            case tapped: {
                if (!tappedHooks.contains(hook)) tappedHooks.add(hook);
                break;
            }
            case released: {
                if (!releasedHooks.contains(hook)) releasedHooks.add(hook);
                break;
            }
        }
    }

    public static void installAxisHook(IFloatInputHook hook) {
        if (!axisHooks.contains(hook)) axisHooks.add(hook);
    }

    public static boolean realIsPressed(KeyCode code) {
        return hooker.fcRealIsPressed(code);
    }

    public static boolean realIsTapped(KeyCode code) {
        return hooker.fcRealIsTapped(code);
    }

    public static boolean realIsReleased(KeyCode code) {
        return hooker.fcRealIsReleased(code);
    }

    public static float getRealAxis(KeyCode code) {
        return hooker.fcRealGetAxis(code);
    }

    public static class fcInputHooker extends KeyboardDevice {
        @Override
        public boolean isPressed(KeyCode key) {
            boolean v = super.isPressed(key);
            for (IBooleanInputHook hook : pressedHooks) v = hook.handle(key, v);
            return v;
        }

        @Override
        public boolean isTapped(KeyCode key) {
            boolean v = super.isTapped(key);
            for (IBooleanInputHook hook : tappedHooks) v = hook.handle(key, v);
            return v;
        }

        @Override
        public boolean isReleased(KeyCode key){
            boolean v = super.isReleased(key);
            for (IBooleanInputHook hook : releasedHooks) v = hook.handle(key, v);
            return v;
        }

        @Override
        public float getAxis(KeyCode keyCode){
            float v = super.getAxis(keyCode);
            for (IFloatInputHook hook : axisHooks) v = hook.handle(keyCode, v);
            return v;
        }

        protected boolean fcRealIsPressed(KeyCode code) {
            return super.isPressed(code);
        }

        protected boolean fcRealIsTapped(KeyCode code) {
            return super.isTapped(code);
        }

        protected boolean fcRealIsReleased(KeyCode code) {
            return super.isReleased(code);
        }

        protected float fcRealGetAxis(KeyCode code) {
            return super.getAxis(code);
        }
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
