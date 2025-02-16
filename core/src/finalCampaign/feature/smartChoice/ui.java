package finalCampaign.feature.smartChoice;

import arc.graphics.*;
import arc.scene.ui.*;
import arc.struct.*;
import finalCampaign.feature.hudUI.*;
import finalCampaign.ui.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.world.*;

public class ui {

    private static roulette choice;

    public static void init() {
        choice = new roulette();
        choice.addRouletteScrollPageSwitchingHandle();
    }

    public static void showRoulette(Seq<Block> choices) {
        choice.clearRoulette();

        for (var block : choices) {
            Image image = new Image(block.fullIcon);

            choice.addRouletteChoice(image, block, block.localizedName, () -> {
                Vars.control.input.block = block;
            });

            image.update(() -> {
                Building core = Vars.player.core();
                Color imgColor = (Vars.state.rules.infiniteResources || (core != null && (core.items.has(block.requirements, Vars.state.rules.buildCostMultiplier) || Vars.state.rules.infiniteResources))) && Vars.player.isBuilder() ? Color.white : Color.gray;
                image.color.set(imgColor);
            });
        }

        choice.showRoulette(fHudUI.topPopupLayer);
    }

    public static void closeRoulette() {
        choice.closeRoulette();
    }
}
