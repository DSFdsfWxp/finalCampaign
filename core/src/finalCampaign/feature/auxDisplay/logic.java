package finalCampaign.feature.auxDisplay;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import finalCampaign.event.*;
import finalCampaign.net.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.input.*;
import mindustry.ui.*;

public class logic {

    private static Team lastTeam;
    private static Seq<BuildPlan> lastPlans = new Seq<>();
    protected static playerBuildPlans plans = new playerBuildPlans();

    public static void updateHovered(fcPlacementFragHoveredUpdateEvent e) {
        if (!fAuxDisplay.enabled)
            return;

        Displayable current = e.current;

        if (current == null && fAuxDisplay.config.displayEnemyInfo && Vars.state.rules.mode() == Gamemode.sandbox) {
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
                current = target;
        }

        if (fAuxDisplay.config.displayTeamPlayerBuildPlan) {
            Displayable target = logic.plans.planAtWorldPos(Core.input.mouseWorldX(), Core.input.mouseWorldY());
            if (target != null)
                current = target;
        }

        if (current != e.current)
            e.replace.get(current);
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

    public static void drawBottom(fcDrawWorldBottomEvent e) {
        if (!fAuxDisplay.enabled || !fAuxDisplay.config.displayTeamPlayerBuildPlan)
            return;

        Eachable<BuildPlan> allPlans = cons -> {
            Vars.control.input.allPlans().each(cons);
            logic.plans.buildPlans.each(cons);
        };

        // plan
        for (var plan : logic.plans.buildPlans) {
            plan.animScale = 1f;

            if (plan.breaking)
                Vars.control.input.drawBreaking(plan);
            else
                plan.block.drawPlan(plan, allPlans, true, 1f);
        }

        // plan top
        for (var plan : logic.plans.buildPlans) {
            if (!plan.breaking) {
                Draw.reset();
                Draw.mixcol(Color.white, 0.24f + Mathf.absin(Time.globalTime, 6f, 0.28f));
                Draw.alpha(1f);
                plan.block.drawPlanConfigTop(plan, allPlans);
            }
        }

        Draw.reset();
    }

    public static void update(fcInputHandleUpdateEvent e) {
        if (!fAuxDisplay.enabled || e.beforeUpdate)
            return;

        if (fAuxDisplay.config.displayBuildCancelTip && Vars.control.input instanceof DesktopInput di) {
            if (di.mode == PlaceMode.placing)
                ui.showToast(true);
            if (di.mode == PlaceMode.breaking)
                ui.showToast(false);
            if (di.mode == PlaceMode.none)
                ui.closeToast();
        }

        if (fAuxDisplay.config.displayTeamPlayerBuildPlan) {
            if (!Vars.player.dead()) {
                var arr1 = Vars.player.unit().plans();
                var arr2 = lastPlans;

                for (var plan : arr1) {
                    if (arr2.contains(plan)) {
                        if (plan.initialized)
                            fcCall.boardcastPlayerPlanRemove(plan.x, plan.y);
                    } else if (!plan.initialized) {
                        fcCall.boardcastPlayerPlanAdd(plan);
                    }
                }

                for (var plan : arr2) {
                    if (!arr1.contains(plan))
                        fcCall.boardcastPlayerPlanRemove(plan.x, plan.y);
                }

                arr2.clear();
                for (var plan : arr1)
                    if (!plan.initialized)
                        arr2.add(plan);
            }
        }
    }

    public static void updateState(EventType.StateChangeEvent e) {
        if (!fAuxDisplay.enabled)
            return;

        if (e.to == GameState.State.menu) {
            lastPlans.clear();
            plans.clear();
        }
    }
}
