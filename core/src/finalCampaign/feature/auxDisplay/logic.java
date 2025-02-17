package finalCampaign.feature.auxDisplay;

import arc.*;
import finalCampaign.event.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.input.*;

public class logic {

    private static Team lastTeam;

    public static void updateHovered(fcPlacementFragHoveredUpdateEvent e) {
        if (!fAuxDisplay.enabled || !fAuxDisplay.config.displayEnemyInfo || Vars.state.rules.mode() != Gamemode.sandbox)
            return;

        if (e.current == null) {
            Unit target = null;

            for (Team team : Team.all) {
                if (team == Vars.player.team())
                    continue;
                Unit unit = Units.closestOverlap(team, Core.input.mouseWorldX(), Core.input.mouseWorldY(), 5f, u -> !u.isLocal() && u.displayable());
                if (unit != null) {
                    target = unit;
                    break;
                }
            }

            if (target != null)
                e.replace.get(target);
        }
    }

    public static void displayEntityInfo(fcEntityDisplayInfoEvent e) {
        if (!fAuxDisplay.enabled || !fAuxDisplay.config.displayEnemyInfo || Vars.state.rules.mode() != Gamemode.sandbox)
            return;

        if (e.targetBuilding != null) {
            if (e.beforeDisplay) {
                lastTeam = e.targetBuilding.team;
                e.targetBuilding.team = Vars.player.team();
            } else {
                if (lastTeam != null) {
                    e.targetBuilding.team = lastTeam;
                    lastTeam = null;
                }
            }
        }
    }

    public static void drawTop(fcDrawWorldTopEvent e) {
        if (!fAuxDisplay.enabled)
            return;

        if (fAuxDisplay.config.displayEnemyInfo && Vars.state.rules.mode() == Gamemode.sandbox) {
            Building building = Vars.world.buildWorld(Core.input.mouseWorldX(), Core.input.mouseWorldY());

            if (building != null && building.team != Vars.player.team()) {
                building.drawSelect();
                if (!building.enabled && building.block.drawDisabled)
                    building.drawDisabled();
            }
        }
    }

    public static void update(fcInputHandleUpdateEvent e) {
        if (!fAuxDisplay.enabled || e.beforeUpdate)
            return;

        if (fAuxDisplay.config.displayBuildCancelTipAndAnimation && Vars.control.input instanceof DesktopInput di) {
            if (di.mode == PlaceMode.placing)
                ui.showToast(true);
            if (di.mode == PlaceMode.breaking)
                ui.showToast(false);
            if (di.mode == PlaceMode.none)
                ui.closeToast();
        }


    }


}
