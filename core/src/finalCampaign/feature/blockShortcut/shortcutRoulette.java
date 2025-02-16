package finalCampaign.feature.blockShortcut;

import arc.*;
import arc.graphics.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import finalCampaign.ui.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.world.*;

public class shortcutRoulette {

    protected static roulette shortcut;
    private static Drawable blank;

    public static void init() {
        shortcut = new roulette();
        blank = new TextureRegionDrawable(Core.atlas.find("clear-effect"));
    }

    public static void rebuild() {
        shortcut.clearRoulette();

        for (var block : fBlockShortcut.blockLst) {
            if (block == null) {
                shortcut.addRouletteChoice(new Image(blank), null, "", () -> false, () -> {});
            } else {
                Image image = new Image(block.fullIcon);

                shortcut.addRouletteChoice(image, block, block.localizedName, () -> !invalidBlock(block), () -> {
                    Vars.control.input.block = block;
                });

                image.update(() -> {
                    Building core = Vars.player.core();
                    Color imgColor = (Vars.state.rules.infiniteResources || (core != null && (core.items.has(block.requirements, Vars.state.rules.buildCostMultiplier) || Vars.state.rules.infiniteResources))) && Vars.player.isBuilder() ? Color.white : Color.gray;
                    image.color.set(imgColor);
                });
            }
        }
    }

    private static boolean invalidBlock(Block block) {
        return !block.isPlaceable() || (!block.unlockedNow() && !((Vars.state == null || Vars.state.rules.infiniteResources) || Vars.state.rules.editor)) || !block.environmentBuildable() || !block.placeablePlayer;
    }
}
