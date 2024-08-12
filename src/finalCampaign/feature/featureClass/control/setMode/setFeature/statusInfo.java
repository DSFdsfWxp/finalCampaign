package finalCampaign.feature.featureClass.control.setMode.setFeature;

import arc.graphics.g2d.*;
import arc.scene.ui.Label;
import arc.scene.ui.layout.*;
import finalCampaign.bundle.*;
import finalCampaign.feature.featureClass.control.setMode.*;
import finalCampaign.patch.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.turrets.BaseTurret.*;
import mindustry.world.blocks.production.Drill.*;
import mindustry.world.blocks.production.Fracker.*;
import mindustry.world.blocks.production.GenericCrafter.*;
import mindustry.world.blocks.production.Incinerator.*;
import mindustry.world.blocks.production.ItemIncinerator.*;
import mindustry.world.blocks.production.Pump.*;
import mindustry.world.blocks.production.Separator.*;
import mindustry.world.blocks.production.WallCrafter.*;
import mindustry.world.meta.*;

public class statusInfo extends iFeature{
    public statusInfo() {
        supportMultiSelect = false;
        category = "basic";
        name = "statusInfo";
    }

    public boolean isSupported(Building[] selected) {
        return selected[0].block.enableDrawStatus && selected[0].block.consumers.length > 0;
    }

    public void buildUI(Building[] selected, Table table, bundleNS bundleNS) {
        Building building = selected[0];
        IFcBuilding fcBuilding = (IFcBuilding) building;

        table.left();
        table.add(new Table() {
            @Override
            public void draw() {
                super.draw();

                float cx = x + width / 2f;
                float cy = y + height / 2f;

                Draw.color(Pal.gray);
                Fill.square(cx, cy, width / 1.414f, 45);
                Draw.color(building.status().color);
                Fill.square(cx, cy, width / 1.414f * 0.6f, 45);
                Draw.color();
            }
        }).left().size(16f).padRight(16f).padLeft(16f);

        Label label = new Label("null");

        Runnable update = () -> {
            String status = "";

            if (building.status() == BlockStatus.noInput) status = "reqMissing";

            if (building.status() == BlockStatus.logicDisable) status = fcBuilding.fcForceDisable() ? "forceDisable" : "logicDisable";

            if (building instanceof GenericCrafterBuild || building instanceof SeparatorBuild) {
                if (building.status() == BlockStatus.active) status = "producing";
                if (building.status() == BlockStatus.noOutput) status = "full";
            }

            if (building instanceof FrackerBuild || building instanceof DrillBuild || building instanceof PumpBuild || building instanceof WallCrafterBuild) {
                if (building.status() == BlockStatus.active) status = "collecting";
                if (building.status() == BlockStatus.noOutput) status = "full";
            }

            if (building instanceof IncineratorBuild || building instanceof ItemIncineratorBuild) {
                if (building.status() == BlockStatus.active) status = "destroying";
                if (building.status() == BlockStatus.noOutput) status = "full";
            }

            if (building instanceof BaseTurretBuild) {
                if (building.status() == BlockStatus.active) status = "firing";
                if (building.status() == BlockStatus.noOutput) status = "targeting";
            }

            if (status.length() == 0) status = building.status().name();

            if (!fcBuilding.fcStatus().equals(status)) fcBuilding.fcStatus(status);
            label.setText(bundleNS.get(status));
        };

        table.add(label).update(l -> update.run());
    }
}
