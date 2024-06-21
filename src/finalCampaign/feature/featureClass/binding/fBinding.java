package finalCampaign.feature.featureClass.binding;

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
        Seq<KeyBind> tmp2 = new Seq<>(tmp.toArray(KeyBind.class));
        for (Object o : binding.values()) tmp2.add((KeyBind) o);
        allKeyBinds = tmp2.toArray(KeyBind.class);
    }

    public static void load() {
        KeyBinds newKeyBinds = new KeyBinds();
        newKeyBinds.setDefaults(allKeyBinds);
        Core.keybinds = newKeyBinds;
        Core.settings.manualSave();
        Core.settings.load();

        Vars.ui.controls = new KeybindDialog();
    }
}
