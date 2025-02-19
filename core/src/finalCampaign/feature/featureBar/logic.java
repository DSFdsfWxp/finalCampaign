package finalCampaign.feature.featureBar;

import arc.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import finalCampaign.event.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.game.*;
import mindustry.input.*;
import mindustry.ui.*;

public class logic {

    private static Table hudOverlayMarkerButtons;
    private static Cell<ImageButton> hudOverlayMarkerSchematicsCell;
    private static ImageButton hudOverlayMarkerSchematicsButton;
    private static ImageButton moreWindowToggler;

    // remove placement buttons in placementUI on desktop
    public static void buildPlacementUI(fcInputHandleBuildPlacementUIEvent event) {
        if (!(Vars.control.input instanceof DesktopInput) || !fFeatureBar.enabled)
            return;

        // in-game editor update
        if ((Version.type.equals("bleeding-edge") && Version.build >= 25329) ||
            Version.number > 7 ||
            (Version.type.equals("release") && Version.build > 146))
                if (Vars.state.isEditor())
                    return;

        Core.app.post(() -> {
            Table parent = (Table) event.table.parent;
            Cell<?> blocksCell = parent.getCell(parent.getChildren().get(0));

            blocksCell.height(242f);
            blocksCell.get().invalidate();
            parent.invalidate();

            event.table.clear();
        });
    }

    public static void stateChanged(EventType.StateChangeEvent event) {
        if (event.from == GameState.State.menu && event.to != GameState.State.menu)
            ui.setup();
    }

    // replace schematics button with moreWindowToggler on mobile
    @SuppressWarnings("unchecked")
    public static void hudBuild(fcHudFragBuildEvent event) {
        Table overlaymarker = event.parent.find("overlaymarker");
        if (overlaymarker == null)
            return;

        Table buttons = overlaymarker.find("mobile buttons");
        hudOverlayMarkerButtons = buttons;
        if (buttons == null)
            return;

        ImageButton schematics = buttons.find("schematics");
        moreWindowToggler = ui.buildMoreWindowToggleButton(buttons, Styles.clearTogglei, 65f, 48f).get();

        if (schematics != null) {
            moreWindowToggler.remove();
            hudOverlayMarkerSchematicsCell = (Cell<ImageButton>) buttons.getCell(schematics);
            hudOverlayMarkerSchematicsButton = hudOverlayMarkerSchematicsCell.get();
            hudOverlayMarkerSchematicsCell.setElement(moreWindowToggler).name("fcFeatureBarMoreWindowToggler");
        }
    }

    public static void restoreHudUI() {
        if (moreWindowToggler == null)
            return;

        if (hudOverlayMarkerSchematicsCell == null) {
            moreWindowToggler.remove();
        } else if (hudOverlayMarkerSchematicsButton != null) {
            hudOverlayMarkerSchematicsCell.setElement(hudOverlayMarkerSchematicsButton).name("schematics");
        }
    }

    public static void patchHudUI() {
        if (moreWindowToggler == null)
            return;

        if (hudOverlayMarkerSchematicsCell == null) {
            hudOverlayMarkerButtons.add(moreWindowToggler);
        } else if (hudOverlayMarkerSchematicsButton != null) {
            hudOverlayMarkerSchematicsCell.setElement(moreWindowToggler).name("fcFeatureBarMoreWindowToggler");
        }
    }
}
