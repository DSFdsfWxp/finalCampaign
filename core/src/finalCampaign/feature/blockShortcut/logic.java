package finalCampaign.feature.blockShortcut;

import arc.*;
import arc.scene.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import finalCampaign.*;
import finalCampaign.event.*;
import finalCampaign.input.*;
import mindustry.*;
import mindustry.world.*;

public class logic {

    public static void buildPlacementFrag(fcPlacementFragBuildEvent e) {
        Table full = (Table) e.parent.getChildren().peek();
        Table topTable = Reflect.get(e.instance, "topTable");
        full.name = "fcPlacementFragment";

        // remove shortcut keys' hint
        Runnable originalUpdate = Reflect.get(Element.class, topTable.getChildren().first(), "update");
        topTable.getChildren().first().update(() -> {
            if (fBlockShortcut.disableGameBlockSelect()) {
                boolean isMobile = Vars.mobile;
                Vars.mobile = true;

                originalUpdate.run();

                Vars.mobile = isMobile;
            } else {
                originalUpdate.run();
            }
        });
    }

    public static void update(fcInputHandleUpdateEvent e) {
        if (e.beforeUpdate || !fBlockShortcut.enabled)
            return;

        for (int i=0; i<fBlockShortcut.keyLst.size; i++)
            if (Core.input.keyTap(fBlockShortcut.keyLst.get(i)))
                checkAndSaveOrSwitchTo(i);

        if (fBlockShortcut.config.enableRoulette) {
            if (!Core.scene.hasDialog() && !Core.scene.hasKeyboard()) {
                if (Core.input.keyDown(fcBindings.blockShortcutRoulette))
                    ui.showRoulette();

                if (Core.input.keyRelease(fcBindings.blockShortcutRoulette))
                    ui.hideRoulette();
            }
        }
    }

    protected static void checkAndSaveOrSwitchTo(int id) {
        if (fBlockShortcut.forceIgnoreCheck)
            return;
        if (Core.scene.hasField() || Core.scene.hasDialog())
            return;

        Block block = Reflect.get(Vars.ui.hudfrag.blockfrag, "menuHoverBlock");
        if (block != null) {
            if (fBlockShortcut.blockLst[id] == block) {
                fBlockShortcut.blockLst[id] = null;
                setting.put("blockShortcut.lst." + id, "");
            } else {
                for (int i=0; i<fBlockShortcut.blockLst.length; i++) if (fBlockShortcut.blockLst[i] == block) fBlockShortcut.clearBlockSlot(i);
                fBlockShortcut.blockLst[id] = block;
                setting.put("blockShortcut.lst." + id, block.name);
            }

            Events.fire(new fBlockShortcut.shortcutChangeEvent(fBlockShortcut.blockLst[id], id));
        } else {
            if (fBlockShortcut.blockLst[id] != null) Vars.control.input.block = Vars.control.input.block == fBlockShortcut.blockLst[id] ? null : fBlockShortcut.blockLst[id];

            if (Vars.control.input.block != null) {
                Vars.ui.hudfrag.blockfrag.currentCategory = Vars.control.input.block.category;
                Reflect.invoke(Vars.ui.hudfrag.blockfrag, "fcRebuildCategory");
            }
        }
    }
}
