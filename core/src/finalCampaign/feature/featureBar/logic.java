package finalCampaign.feature.featureBar;

import arc.scene.ui.layout.*;
import finalCampaign.event.*;
import mindustry.*;
import mindustry.core.*;
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

    public static void buildUI(fcInputHandleBuildUIEvent event) {
        if (Vars.control.input instanceof MobileInput) {
            for (int i = 0; i < 3; i++)
                event.group.getChildren().pop();
        }
    }
}
