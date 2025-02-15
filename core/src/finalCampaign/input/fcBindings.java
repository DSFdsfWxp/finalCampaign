package finalCampaign.input;

import arc.*;
import arc.KeyBinds.*;
import arc.input.InputDevice.*;
import arc.struct.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.input.*;
import mindustry.ui.dialogs.*;
import arc.input.*;

public enum fcBindings implements KeyBind {

    switchLensMode(KeyCode.f4, "finalCampaign"),
    boostCamera(KeyCode.shiftLeft),
    slowCamera(KeyCode.controlLeft),
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

    fcBindings(KeybindValue defaultValue, String category) {
        this.defaultValue = defaultValue;
        this.category = category;
    }

    fcBindings(KeybindValue defaultValue) {
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

    public static void load() {
        Seq<KeyBind> tmp = new Seq<>();
        for (Object o : Binding.values()) tmp.add((KeyBind) o);
        for (Object o : values()) tmp.add((KeyBind) o);
        KeyBinds newKeyBinds = new KeyBinds();
        newKeyBinds.setDefaults(tmp.toArray(KeyBind.class));

        Core.keybinds = newKeyBinds;
        Reflect.invoke(newKeyBinds, "load");
        Vars.ui.controls = new KeybindDialog();
    }
}