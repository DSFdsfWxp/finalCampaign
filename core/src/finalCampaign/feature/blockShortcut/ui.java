package finalCampaign.feature.blockShortcut;

import arc.scene.ui.layout.*;
import finalCampaign.feature.hudUI.*;

public class ui {

    private static Table shortcut;
    private static Cell<Table> shortcutCell;

    public static void init() {
        shortcut = new shortcutTable();
        shortcutRoulette.init();
    }

    public static void showRoulette() {
        shortcutRoulette.rebuild();
        shortcutRoulette.shortcut.showRoulette(fHudUI.topPopupLayer);
    }

    public static void hideRoulette() {
        shortcutRoulette.shortcut.closeRoulette();
    }

    public static void setupShortcutTable() {
        shortcutCell = fHudUI.fixedLayer.bottomRight.add(shortcut);
        if (!fBlockShortcut.enabled)
            shortcutCell.setElement(null);
    }

    public static void updateShortcutTableUpdate() {
        if (fBlockShortcut.enabled)
            shortcutCell.setElement(shortcut);
        else
            shortcutCell.setElement(null);
    }
}
