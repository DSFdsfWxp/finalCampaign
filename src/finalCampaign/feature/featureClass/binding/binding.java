package finalCampaign.feature.featureClass.binding;

import arc.KeyBinds.*;
import arc.input.InputDevice.*;
import arc.input.KeyCode;

public enum binding implements KeyBind {

    freeVision(KeyCode.f4, "finalCampaign"),
    roulette(KeyCode.tab),

    blockShortcut_1(KeyCode.num1),
    blockShortcut_2(KeyCode.num2),
    blockShortcut_3(KeyCode.num3),
    blockShortcut_4(KeyCode.num4),
    blockShortcut_5(KeyCode.num5),
    blockShortcut_6(KeyCode.num6),
    blockShortcut_7(KeyCode.num7),
    blockShortcut_8(KeyCode.num8),
    blockShortcut_9(KeyCode.num9),
    blockShortcut_10(KeyCode.num0),

    setMode(KeyCode.controlLeft)
    ;
    
    private final KeybindValue defaultValue;
    private final String category;

    binding(KeybindValue defaultValue, String category) {
        this.defaultValue = defaultValue;
        this.category = category;
    }

    binding(KeybindValue defaultValue) {
        this(defaultValue, null);
    }

    @Override
    public KeybindValue defaultValue(DeviceType type) {
        return defaultValue;
    }

    @Override
    public String category() {
        return category;
    }
}
