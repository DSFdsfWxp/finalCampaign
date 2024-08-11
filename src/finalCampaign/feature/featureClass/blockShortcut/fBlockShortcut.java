package finalCampaign.feature.featureClass.blockShortcut;

import finalCampaign.*;
import finalCampaign.feature.featureClass.binding.*;
import finalCampaign.feature.featureClass.fcDesktopInput.*;
import finalCampaign.feature.featureClass.tuner.*;
import finalCampaign.feature.featureClass.tuner.fTuner.*;
import mindustry.*;
import mindustry.core.GameState.*;
import mindustry.game.EventType.*;
import mindustry.input.*;
import mindustry.world.*;
import arc.*;
import arc.KeyBinds.*;
import arc.struct.*;
import arc.util.*;

public class fBlockShortcut {

    private static Block[] blockLst;
    private static Seq<KeyBind> keyLst;
    private static boolean isOn;
    private static config config;
    private static Binding[] bindings;
    public static boolean forceIgnoreCheck;

    public static class config {
        public boolean disableGameBlockSelect = false;
    }

    public static void init() {
        blockLst = new Block[10];
        keyLst = new Seq<>();

        keyLst.add(binding.blockShortcut_1);
        keyLst.add(binding.blockShortcut_2);
        keyLst.add(binding.blockShortcut_3);
        keyLst.add(binding.blockShortcut_4);
        keyLst.add(binding.blockShortcut_5);
        keyLst.add(binding.blockShortcut_6);
        keyLst.add(binding.blockShortcut_7);
        keyLst.add(binding.blockShortcut_8);
        keyLst.add(binding.blockShortcut_9);
        keyLst.add(binding.blockShortcut_10);

        isOn = false;
        forceIgnoreCheck = false;
        config = new config();

        String[] subFeatures = new String[] {"roulette", "shortcut"};
        Events.on(stateChangeEvent.class, e -> {
            boolean isOn = false;
            for (String sub : subFeatures) if (fTuner.isOn(sub)) isOn = true;
            if (isOn != fBlockShortcut.isOn && config.disableGameBlockSelect) {
                if (isOn) {
                    bindings = Reflect.get(Vars.ui.hudfrag.blockfrag, "blockSelect");
                    Reflect.set(Vars.ui.hudfrag.blockfrag, "blockSelect", new Binding[] {});
                } else {
                    if (bindings != null) Reflect.set(Vars.ui.hudfrag.blockfrag, "blockSelect", bindings);
                }
                fBlockShortcut.isOn = isOn;
            }
        });
    }

    public static void load() {
        for (int i=0; i<10; i++) blockLst[i] = Vars.content.block(setting.getAndCast("blockShortcut.lst." + Integer.toString(i), ""));

        fTuner.add("blockShortcut", config);

        fFcDesktopInput.addBindingHandle(() -> {
            for (int i=0; i<keyLst.size; i++) if (Core.input.keyTap(keyLst.get(i))) checkAndSaveOrSwitchTo(i);
        });

        Events.on(StateChangeEvent.class, e -> {
            if (e.to == State.playing) {
                Events.fire(new stateChangeEvent("", false));
            }
        });
    }

    private static void checkAndSaveOrSwitchTo(int id) {
        if (forceIgnoreCheck) return;
        if (Core.scene.hasField() || Core.scene.hasDialog()) return;
        
        Block block = Reflect.get(Vars.ui.hudfrag.blockfrag, "menuHoverBlock");
        if (block != null) {
            if (blockLst[id] == block) {
                blockLst[id] = null;
                setting.put("blockShortcut.lst." + Integer.toString(id), "");
            } else {
                for (int i=0; i<blockLst.length; i++) if (blockLst[i] == block) clearBlockSlot(i);
                blockLst[id] = block;
                setting.put("blockShortcut.lst." + Integer.toString(id), block.name);
            }
            
            Events.fire(new shortcutChangeEvent(blockLst[id], id));
        } else {
            if (blockLst[id] != null) Vars.control.input.block = Vars.control.input.block == blockLst[id] ? null : blockLst[id];
        }
    }

    public static Block getShortcutSlot(int id) {
        return blockLst[id];
    }

    public static void clearBlockSlot(int id) {
        blockLst[id] = null;
        setting.put("blockShortcut.lst." + Integer.toString(id), "");
        Events.fire(new shortcutChangeEvent(null, id));
    }

    public static class shortcutChangeEvent {
        public final Block block;
        public final int id;
        
        public shortcutChangeEvent(Block block, int id) {
            this.block = block;
            this.id = id;
        }
    }

    public static boolean isOn() {
        return isOn;
    }

    public static boolean disabledGameBlockSelect() {
        return config.disableGameBlockSelect;
    }
}
