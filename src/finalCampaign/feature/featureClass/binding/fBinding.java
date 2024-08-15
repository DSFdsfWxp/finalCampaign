package finalCampaign.feature.featureClass.binding;

import java.lang.reflect.*;
import arc.*;
import arc.struct.*;
import arc.util.Reflect;
import arc.KeyBinds.*;
import arc.input.InputDevice.*;
import arc.input.*;
import mindustry.*;
import mindustry.input.*;
import mindustry.ui.dialogs.*;

public class fBinding {

    public static KeyBind[] allKeyBinds;

    public static boolean supported() {
        return !Vars.headless;
    }
    
    public static void init() {
        Seq<KeyBind> tmp = new Seq<>(Binding.values());
        tmp = new Seq<>(tmp.toArray(KeyBind.class));
        for (Object o : binding.values()) tmp.add((KeyBind) o);
        allKeyBinds = tmp.toArray(KeyBind.class);
    }

    public static void load() throws Exception {
        KeyBinds newKeyBinds = new KeyBinds();
        newKeyBinds.setDefaults(allKeyBinds);
        Core.keybinds = newKeyBinds;
        Method load = KeyBinds.class.getDeclaredMethod("load");
        load.setAccessible(true);
        load.invoke(newKeyBinds);

        Vars.ui.controls = new KeybindDialog();
    }

    public static void clear(String name) {
        for (KeyBind bind : allKeyBinds) if (bind.name().equals(name)) clear(bind, false);
    }

    public static void clear(KeyBind name, boolean reload) {
        Core.keybinds.get(name).key = KeyCode.unknown;
        Reflect.invoke(Core.keybinds, "save");
        for (DeviceType type : DeviceType.values()) {
            Core.settings.put("keybind-default-" + type.name() + "-" + name.name() + "-key", KeyCode.unknown.ordinal());
        }
        if (reload) {
            try {
                load();
            } catch(Exception ignore) {}
        }
    }
}
