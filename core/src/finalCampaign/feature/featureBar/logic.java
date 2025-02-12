package finalCampaign.feature.featureBar;

import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import finalCampaign.event.*;
import finalCampaign.feature.hudUI.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.game.*;
import mindustry.input.*;

public class logic {
    public static void buildPlacementUI(fcInputHandleBuildPlacementUIEvent event) {
        if (!(Vars.control.input instanceof DesktopInput))
            return;

        // in-game editor update
        if ((Version.type.equals("bleeding-edge") && Version.build >= 25329) ||
            Version.number > 7 ||
            (Version.type.equals("release") && Version.build > 146))
                if (Vars.state.isEditor())
                    return;

        Table parent = (Table) event.table.parent;
        Cell<?> blocksCell = parent.getCell(parent.getChildren().get(0));

        blocksCell.height(242f);
        blocksCell.get().invalidate();
        parent.invalidate();

        event.table.remove();
    }

    public static void patchUI() {
        var originalVisibility = fHudUI.fixedLayer.commandModeButtonArea.visibility;
        fHudUI.fixedLayer.commandModeButtonArea.visibility = () -> !ui.bar.visible && Vars.control.input.commandMode && originalVisibility.get();
    }

    public static void stateChanged(EventType.StateChangeEvent event) {
        if (event.from == GameState.State.menu && event.to != GameState.State.menu)
            ui.setup();
    }

    public static void hudBuild(fcHudFragBuildEvent event) {
        Table overlaymarker = event.parent.find("overlaymarker");
        if (overlaymarker == null)
            return;

        Table buttons = overlaymarker.find("mobile buttons");
        if (buttons == null)
            return;

        ImageButton schematics = buttons.find("schematics");

    }
}
