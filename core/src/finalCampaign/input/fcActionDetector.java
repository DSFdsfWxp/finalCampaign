package finalCampaign.input;

import arc.*;
import arc.input.*;
import finalCampaign.event.*;
import finalCampaign.util.*;

public class fcActionDetector implements InputProcessor {
    private final static float longPressTime = 500f;
    private final static float comboTime = 500f;
    private final fcInputKeyComboTapEvent keyComboTapEvent;

    private static timer[] keyTimers;
    private static int[] keyComboCnts;

    public fcActionDetector() {
        int keyCnt = KeyCode.values().length;
        keyTimers = new timer[keyCnt];
        keyComboCnts = new int[keyCnt];
        keyComboTapEvent = new fcInputKeyComboTapEvent();
        for (int i = 0; i < keyCnt; i++) {
            keyTimers[i] = new timer();
            keyComboCnts[i] = 0;
        }
    }

    @Override
    public boolean keyDown(KeyCode keycode) {
        timer keyTimer = keyTimers[keycode.ordinal()];

        if (keyTimer.marked()) {
            if (keyTimer.msTime() > comboTime) {
                keyComboCnts[keycode.ordinal()] = 0;
            } else {
                keyComboCnts[keycode.ordinal()] ++;
            }
            keyTimer.mark();
        }

        return false;
    }

    @Override
    public boolean keyUp(KeyCode keycode) {
        timer keyTimer = keyTimers[keycode.ordinal()];

        if (keyTimer.marked()) {
            if (keyComboCnts[keycode.ordinal()] > 1) {
                keyComboTapEvent.form(keycode, keyComboCnts[keycode.ordinal()]);
                Events.fire(keyComboTapEvent);
                keyComboCnts[keycode.ordinal()] = 0;
            }
            keyTimer.mark();
        }

        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, KeyCode button) {
        keyDown(button);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, KeyCode button) {
        keyUp(button);
        return false;
    }

    public static boolean isLongPressing(KeyBinds.KeyBind bind, int combo) {
        return isLongPressing(Core.keybinds.get(bind).key, combo);
    }

    public static boolean isLongPressing(KeyCode keycode, int combo) {
        return Core.input.keyDown(keycode) && keyTimers[keycode.ordinal()].marked()
                && keyTimers[keycode.ordinal()].msTime() > longPressTime
                && keyComboCnts[keycode.ordinal()] == combo;
    }

    public static boolean isLongPressing(KeyBinds.KeyBind bind) {
        return isLongPressing(Core.keybinds.get(bind).key);
    }

    public static boolean isLongPressing(KeyCode keycode) {
        return isLongPressing(keycode, 0);
    }
}
