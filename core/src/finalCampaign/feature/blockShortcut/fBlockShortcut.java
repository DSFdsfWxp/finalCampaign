package finalCampaign.feature.blockShortcut;

import finalCampaign.*;
import finalCampaign.event.*;
import finalCampaign.feature.tuner.*;
import finalCampaign.feature.tuner.fTuner.*;
import finalCampaign.input.*;
import mindustry.*;
import mindustry.core.GameState.*;
import mindustry.game.EventType.*;
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
    public static boolean forceIgnoreCheck;

    public static class config {
        public boolean disableGameBlockSelect = false;
    }

    public static boolean supported() {
        return !Vars.headless;
    }

    public static void init() {
        blockLst = new Block[10];
        keyLst = new Seq<>();

        keyLst.add(fcBindings.blockShortcut_1);
        keyLst.add(fcBindings.blockShortcut_2);
        keyLst.add(fcBindings.blockShortcut_3);
        keyLst.add(fcBindings.blockShortcut_4);
        keyLst.add(fcBindings.blockShortcut_5);
        keyLst.add(fcBindings.blockShortcut_6);
        keyLst.add(fcBindings.blockShortcut_7);
        keyLst.add(fcBindings.blockShortcut_8);
        keyLst.add(fcBindings.blockShortcut_9);
        keyLst.add(fcBindings.blockShortcut_10);

        isOn = false;
        forceIgnoreCheck = false;
        config = new config();

        String[] subFeatures = new String[] {"roulette", "shortcut"};
        Events.on(stateChangeEvent.class, e -> {
            boolean isOn = false;
            for (String sub : subFeatures) if (fTuner.isOn(sub)) isOn = true;
            if (isOn != fBlockShortcut.isOn && config.disableGameBlockSelect)
                fBlockShortcut.isOn = isOn;
        });
    }

    public static void load() {
        for (int i=0; i<10; i++) blockLst[i] = Vars.content.block(setting.getAndCast("blockShortcut.lst." + Integer.toString(i), ""));

        fTuner.add("blockShortcut", config);

        Events.on(fcInputHandleUpdateEvent.class, event -> {
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
            
            if (Vars.control.input.block != null) {
                Vars.ui.hudfrag.blockfrag.currentCategory = Vars.control.input.block.category;
                Reflect.invoke(Vars.ui.hudfrag.blockfrag, "fcRebuildCategory");
            }
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

    public static boolean disableGameBlockSelect() {
        return config == null ? false : config.disableGameBlockSelect;
    }
}
