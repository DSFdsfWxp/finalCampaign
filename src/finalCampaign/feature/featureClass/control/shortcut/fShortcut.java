package finalCampaign.feature.featureClass.control.shortcut;

import arc.*;
import arc.scene.*;
import arc.scene.ui.layout.*;
import arc.util.Reflect;
import finalCampaign.feature.featureClass.tuner.*;
import finalCampaign.feature.featureClass.tuner.fTuner.*;
import mindustry.*;
import mindustry.game.EventType.*;

public class fShortcut {
    private static config config;
    private static boolean enabled;
    private static shortcutTable table;
    private static Element placement;
    
    public static class config {
        public uiPosition position;
    }

    public static void init() {
        config = new config();
        enabled = false;
    }

    public static void load() throws Exception {
        placement = ((Table) Reflect.get(Vars.ui.hudfrag.blockfrag, "toggler")).getChildren().get(0);

        Events.on(WorldLoadEvent.class, event -> {
            Core.app.post(() -> {
                placement = ((Table) Reflect.get(Vars.ui.hudfrag.blockfrag, "toggler")).getChildren().get(0);
            });
        });

        Events.on(UnlockEvent.class, event -> {
            placement = ((Table) Reflect.get(Vars.ui.hudfrag.blockfrag, "toggler")).getChildren().get(0);
        });

        config.position = new uiPosition(() -> fShortcut.placement.x, () -> fShortcut.placement.y, true);
        
        enabled = fTuner.add("shortcut", false, config, v -> enabled = v);

        table = new shortcutTable();
        Vars.ui.hudGroup.addChild(table);
    }

    public static boolean isOn() {
        return enabled;
    }

    public static float getX() {
        return config.position.getX();
    }

    public static float getY() {
        return config.position.getY();
    }
}
