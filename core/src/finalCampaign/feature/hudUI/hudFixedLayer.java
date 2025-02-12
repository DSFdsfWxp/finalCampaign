package finalCampaign.feature.hudUI;

import arc.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import finalCampaign.event.*;
import mindustry.*;
import mindustry.input.*;

public class hudFixedLayer {
    public Table layer;

    public Table info, placement, map;
    public Table top, topRight;
    public Table left;
    public Table centerBottom;
    public Table bottom, bottomLeft, bottomRight;
    // mobile only
    public @Nullable Table schematicControlArea, commandModeButtonArea, buildingCancelButtonArea;

    public final float topMinWidth = 600f;
    public final float bottomMinWidth = 300f;

    private Table topArea, bottomArea;
    private Table topAreaLeftPlaceholder;
    private Table bottomLeftBlock;
    private Table bottomLeftBlockPlaceholder;

    private Cell<Table> infoCell, topAreaLeftPlaceholderCell, topRightCell, mapCell;
    private Cell<Table> bottomLeftBlockCell, bottomRightCell, placementCell;
    private Cell<Table> bottomLeftBlockPlaceholdCell, bottomLeftCell;

    public void init() {
        Events.on(fcInputHandleBuildPlacementUIEvent.class, this::buildPlacementUI);
        Events.on(fcInputHandleBuildUIEvent.class, this::buildUI);
        Events.on(fcHudFragBuildEvent.class, this::buildUI);
        Events.on(fcPlacementFragBuildEvent.class, this::buildUI);

        layer = new Table();
        layer.name = "fcHudUIFixedLayer";

        layer.table(u -> {
            topArea = u;

            infoCell = u.table();
            info = infoCell.get();

            topAreaLeftPlaceholderCell = u.table();
            topAreaLeftPlaceholder = topAreaLeftPlaceholderCell.get();

            top = u.table().growX().minWidth(topMinWidth).get();

            topRightCell = u.table();
            topRight = topRightCell.get();

            mapCell = u.table();
            map = mapCell.get();
        }).growX().row();

        layer.table(d -> {
            bottomArea = d;

            bottomLeftBlockCell = d.table(l -> {
                bottomLeftBlock = l;

                bottomLeftBlockPlaceholdCell = l.table().growX();
                bottomLeftBlockPlaceholder = bottomLeftBlockPlaceholdCell.get();
                l.row();
                left = l.table().growX().get();
                l.row();
                bottomLeftCell = l.table().growX();
                bottomLeft = bottomLeftCell.get();
            }).growY();

            d.table(c -> {
                centerBottom = c.table().grow().get();
                c.row();
                bottom = c.table().minWidth(bottomMinWidth).get();
            }).grow();

            bottomRightCell = d.table().growY();
            bottomRight = bottomRightCell.get();

            placementCell = d.table().growY();
            placement = placementCell.get();
        }).grow();

        top.top();
        topRight.top().right();
        left.left();
        centerBottom.bottom();
        bottom.bottom();
        bottomLeft.bottom().left();
        bottomRight.bottom().right();

        layer.update(() -> {
            float topAreaHeight = topArea.getHeight();
            float topAreaWidth = topArea.getWidth();
            float bottomAreaHeight = bottomArea.getHeight();
            float bottomAreaWidth = bottomArea.getWidth();
            float targetPlaceholdA, targetPlaceholdB;

            // top
            {
                float infoWidth = infoCell.minWidth();
                float topWidth = top.getPrefWidth();
                float topRightWidth = topRight.getPrefWidth();
                float mapWidth = map.getWidth();

                targetPlaceholdA = targetPlaceholdB = (topAreaWidth - topWidth) / 2f;
                targetPlaceholdA -= infoWidth;
                targetPlaceholdB -= mapWidth + topRightWidth;

                if (targetPlaceholdA < 0)
                    targetPlaceholdB += targetPlaceholdA;
                if (targetPlaceholdB < 0)
                    targetPlaceholdA += targetPlaceholdB;

                targetPlaceholdA = Mathf.maxZero(targetPlaceholdA);
                targetPlaceholdB = Mathf.maxZero(targetPlaceholdB);

                updateWidth(infoCell, targetPlaceholdA);
                updateWidth(topRightCell, topRightWidth + targetPlaceholdB);
            }

            // bottom
            {
                float bottomLeftBlockWidth = bottomLeftBlock.getPrefWidth();
                float bottomWidth = bottom.getPrefWidth();
                float bottomRightWidth = bottomRight.getPrefWidth();
                float placementWidth = placement.getPrefWidth();

                targetPlaceholdA = targetPlaceholdB = (bottomAreaWidth - bottomWidth) / 2f;
                targetPlaceholdA -= bottomLeftBlockWidth;
                targetPlaceholdB -= bottomRightWidth + placementWidth;

                if (targetPlaceholdA < 0)
                    targetPlaceholdB += targetPlaceholdA;
                if (targetPlaceholdB < 0)
                    targetPlaceholdA += targetPlaceholdB;

                targetPlaceholdA = Mathf.maxZero(targetPlaceholdA);
                targetPlaceholdB = Mathf.maxZero(targetPlaceholdB);

                updateWidth(bottomLeftBlockCell, bottomLeftBlockWidth + targetPlaceholdA);
                updateWidth(bottomRightCell, bottomRightWidth + targetPlaceholdB);
            }

            // top
            {
                float leftHeight = left.getPrefHeight();
                float bottomLeftHeight = bottomLeft.getPrefHeight();

                targetPlaceholdA = targetPlaceholdB = (topAreaHeight + bottomAreaHeight - leftHeight) / 2f;
                targetPlaceholdA -= topAreaHeight;
                targetPlaceholdB -= bottomLeftHeight;

                if (targetPlaceholdA < 0)
                    targetPlaceholdB += targetPlaceholdA;
                if (targetPlaceholdB < 0)
                    targetPlaceholdA += targetPlaceholdB;

                targetPlaceholdA = Mathf.maxZero(targetPlaceholdA);
                targetPlaceholdB = Mathf.maxZero(targetPlaceholdB);

                updateHeight(bottomLeftBlockPlaceholdCell, targetPlaceholdA);
                updateHeight(bottomLeftCell, bottomLeftHeight + targetPlaceholdB);
            }
        });
    }

    public void setup(Group parent) {
        layer.setFillParent(true);
        parent.addChild(layer);
    }

    private void buildPlacementUI(fcInputHandleBuildPlacementUIEvent event) {
        if (!(Vars.control.input instanceof DesktopInput))
            return;

        Table parent = (Table) event.table.parent;
        parent.setHeight(parent.getHeight() + event.table.getHeight());
        event.table.remove();
    }

    private void buildUI(fcInputHandleBuildUIEvent event) {
        if (Vars.control.input instanceof DesktopInput) {
            var children = event.group.getChildren();

            for (int i = 1; i <= 2; i++) {
                Table t = (Table) children.get(children.size - i);
                Table pane = (Table) t.getChildren().pop();

                Cell<Table> tphc = t.table();
                t.row();
                t.add(pane);

                tphc.update(tph -> {
                    float dst = centerBottom.getPrefHeight() / Scl.scl() + 4f;
                    updateHeight(tphc, dst);
                });
            }
        } else {
            schematicControlArea = (Table) event.group.getChildren().pop();
            commandModeButtonArea = (Table) event.group.getChildren().pop();
            buildingCancelButtonArea = (Table) event.group.getChildren().pop();

            var cancelButton = buildingCancelButtonArea.getChildren().pop();
            Cell<Table> phc = buildingCancelButtonArea.table();
            buildingCancelButtonArea.row();
            buildingCancelButtonArea.add(cancelButton);

            phc.update(pht -> {
                float dst = bottomLeft.getPrefHeight() / Scl.scl();
                updateHeight(phc, dst);
            });

            event.group.getChildren().add(buildingCancelButtonArea);
            event.group.getChildren().add(commandModeButtonArea);
            event.group.getChildren().add(schematicControlArea);
        }
    }

    private void buildUI(fcHudFragBuildEvent event) {
        Table gameMiniMap = event.parent.find("minimap/position");
        Table gameInfo = event.parent.find("overlaymarker");

        map.update(() -> updateSize(mapCell, gameMiniMap.getWidth(), gameMiniMap.getHeight()));
        info.update(() -> updateSize(infoCell, gameInfo.getWidth(), gameInfo.getHeight()));
    }

    private void buildUI(fcPlacementFragBuildEvent event) {
        Table gamePlacement = Reflect.get(event.instance, "toggler");
        placement.update(() -> updateWidth(placementCell, gamePlacement.getWidth()));
    }

    private void updateWidth(Cell<?> cell, float width) {
        width /= Scl.scl();
        if (cell.minWidth() == width)
            return;
        cell.minWidth(width);
        cell.get().invalidate();
        cell.get().parent.invalidate();
    }

    private void updateHeight(Cell<?> cell, float height) {
        height /= Scl.scl();
        if (cell.minHeight() == height)
            return;
        cell.minHeight(height);
        cell.get().invalidate();
        cell.get().parent.invalidate();
    }

    private void updateSize(Cell<?> cell, float width, float height) {
        updateWidth(cell, width);
        updateHeight(cell, height);
    }
}
