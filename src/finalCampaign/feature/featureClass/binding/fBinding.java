package finalCampaign.feature.featureClass.binding;

import java.lang.reflect.*;
import arc.*;
import arc.struct.*;
import arc.KeyBinds.*;
import mindustry.*;
import mindustry.input.*;
import mindustry.ui.dialogs.*;

public class fBinding {

    public static KeyBind[] allKeyBinds;
    
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
}
