package finalCampaign.feature.roulette;

import arc.*;
import arc.math.geom.*;
import arc.util.*;
import finalCampaign.event.*;
import finalCampaign.feature.setMode.*;
import finalCampaign.feature.tuner.*;
import finalCampaign.feature.tuner.fTuner.*;
import finalCampaign.input.*;
import mindustry.*;

public class fRoulette {
    private static boolean inited = false;
    private static boolean on;
    private static boolean enabled;
    private static config config;
    private static rouletteFragment frag;

    public static class config {
        public floatSlider scale = new floatSlider(1f, 3f, 0.8f, 0.1f);
        public boolean fourSlot = false;
    }

    public static boolean supported() {
        return !Vars.headless && !Vars.mobile;
    }

    public static void lateInit() {
        on = false;
        enabled = false;
        config = new config();
    }

    public static boolean isOn() {
        return inited && on && enabled && !Vars.control.input.commandMode && !Vars.ui.minimapfrag.shown() && !fSetMode.isOn();
    }

    public static void lateLoad() {
        enabled = fTuner.add("roulette", false, config, v -> enabled = v);
        frag = new rouletteFragment();

        Events.on(fcInputHandleUpdateEvent.class, event -> {
            if (Core.input.keyDown(fcBindings.roulette) && !Core.scene.hasField() && !Core.scene.hasDialog() && !fSetMode.isOn()) {
                if (!on) {
                    on = true;
                    Vec2 mPos = Core.input.mouse();
                    frag.mouseX = mPos.x;
                    frag.mouseY = mPos.y;
                    Vars.ui.hudGroup.addChild(frag);
                    frag.added(0.2f);
                }
            }

            if (Core.input.keyRelease(fcBindings.roulette)) {
                on = false;
                frag.remove(0.2f);
                if (frag.selectedBlock != null) {
                    Vars.control.input.block = frag.selectedBlock;
                    Vars.ui.hudfrag.blockfrag.currentCategory = frag.selectedBlock.category;
                    Reflect.invoke(Vars.ui.hudfrag.blockfrag, "fcRebuildCategory");
                }
            }
        });

        inited = true;
    }

    public static float scale() {
        return config.scale.value;
    }

    public static boolean usingFourSlot() {
        return config.fourSlot;
    }
}
