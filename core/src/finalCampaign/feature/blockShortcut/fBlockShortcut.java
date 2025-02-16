package finalCampaign.feature.blockShortcut;

import finalCampaign.*;
import finalCampaign.event.*;
import finalCampaign.feature.tuner.*;
import finalCampaign.input.*;
import mindustry.*;
import mindustry.world.*;
import arc.*;
import arc.KeyBinds.*;
import arc.struct.*;

public class fBlockShortcut {

    protected static Block[] blockLst;
    protected static Seq<KeyBind> keyLst;
    protected static boolean enabled;
    protected static tunerConfig config;
    protected static boolean forceIgnoreCheck;

    public static class tunerConfig {
        public boolean disableGameBlockSelect = false;
        public boolean enableRoulette = false;
    }

    public static boolean supported() {
        return !Vars.headless;
    }

    public static void lateInit() {
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

        forceIgnoreCheck = false;
        config = new tunerConfig();

        ui.init();
    }

    public static void lateLoad() {
        for (int i=0; i<10; i++) blockLst[i] = Vars.content.block(setting.getAndCast("blockShortcut.lst." + i, ""));

        enabled = fTuner.add("blockShortcut", false, config, v -> {
            enabled = v;
            ui.updateShortcutTableUpdate();
        });

        Events.on(fcInputHandleUpdateEvent.class, logic::update);
    }

    public static void earlyLoad() {
        Events.on(fcPlacementFragBuildEvent.class, logic::buildPlacementFrag);
    }

    public static void buildUI() {
        ui.setupShortcutTable();
    }

    public static Block getShortcutSlot(int id) {
        return blockLst[id];
    }

    public static void clearBlockSlot(int id) {
        blockLst[id] = null;
        setting.put("blockShortcut.lst." + id, "");
        Events.fire(new shortcutChangeEvent(null, id));
    }

    // record class is not available on java 8
    public static class shortcutChangeEvent {
        public final Block block;
        public final int id;

        public shortcutChangeEvent(Block block, int id) {
            this.block = block;
            this.id = id;
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean disableGameBlockSelect() {
        return config != null && config.disableGameBlockSelect;
    }
}
