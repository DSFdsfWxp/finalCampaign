package finalCampaign.feature.featureClass.control.roulette;

import arc.*;
import arc.math.geom.*;
import arc.util.Reflect;
import finalCampaign.feature.featureClass.binding.*;
import finalCampaign.feature.featureClass.fcDesktopInput.*;
import finalCampaign.feature.featureClass.tuner.*;
import finalCampaign.feature.featureClass.tuner.fTuner.*;
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

    public static void init() {
        on = false;
        enabled = false;
        config = new config();
    }

    public static boolean isOn() {
        return inited && on && enabled && !Vars.control.input.commandMode;
    }

    public static void load() {
        enabled = fTuner.add("roulette", false, config, v -> enabled = v);

        fFcDesktopInput.addBindingHandle(() -> {
            if (Core.input.keyDown(binding.roulette) && !Core.scene.hasField() && !Core.scene.hasDialog()) {
                if (!on) {
                    on = true;
                    Vec2 mPos = Core.input.mouse();
                    if (frag == null) frag = new rouletteFragment();
                    frag.mouseX = mPos.x;
                    frag.mouseY = mPos.y;
                    Vars.ui.hudGroup.addChild(frag);
                    frag.added(0.2f);
                }
            }

            if (Core.input.keyRelease(binding.roulette)) {
                on = false;
                frag.remove(0.2f);
                if (frag.selectedBlock != null) {
                    Vars.control.input.block = frag.selectedBlock;
                    Vars.ui.hudfrag.blockfrag.currentCategory = frag.selectedBlock.category;
                    Reflect.invoke(Vars.ui.hudfrag.blockfrag, "rebuildCategory");
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
