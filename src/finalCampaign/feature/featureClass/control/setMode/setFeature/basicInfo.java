package finalCampaign.feature.featureClass.control.setMode.setFeature;

import arc.*;
import arc.graphics.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import finalCampaign.bundle.*;
import finalCampaign.feature.featureClass.control.setMode.*;
import mindustry.Vars;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.meta.StatUnit;

public class basicInfo extends iFeature {
    public basicInfo() {
        category = "basic";
        name = "basicInfo";
        supportMultiSelect = false;
    }

    public boolean isSupported(Building[] selected) {
        return true;
    }

    public void buildUI(Building[] selected, Table table, bundleNS bundleNS) {
        Building building = selected[0];

        table.table(bars -> {
            bars.defaults().growX().height(18f).pad(4);

            building.displayBars(bars);
        }).growX();
        table.row();
        table.table(building::displayConsumption).growX();

        boolean displayFlow = (building.block.category == Category.distribution || building.block.category == Category.liquid) && building.block.displayFlow;

        if(displayFlow){
            String ps = " " + StatUnit.perSecond.localized();

            var flowItems = building.flowItems();

            if(flowItems != null){
                table.row();
                table.left();
                table.table(l -> {
                    Bits current = new Bits();

                    Runnable rebuild = () -> {
                        l.clearChildren();
                        l.left();
                        for(Item item : Vars.content.items()){
                            if(flowItems.hasFlowItem(item)){
                                l.image(item.uiIcon).scaling(Scaling.fit).padRight(3f);
                                l.label(() -> flowItems.getFlowRate(item) < 0 ? "..." : Strings.fixed(flowItems.getFlowRate(item), 1) + ps).color(Color.lightGray);
                                l.row();
                            }
                        }
                    };

                    rebuild.run();
                    l.update(() -> {
                        for(Item item : Vars.content.items()){
                            if(flowItems.hasFlowItem(item) && !current.get(item.id)){
                                current.set(item.id);
                                rebuild.run();
                            }
                        }
                    });
                }).left();
            }

            if(building.liquids != null){
                table.row();
                table.left();
                table.table(l -> {
                    Bits current = new Bits();

                    Runnable rebuild = () -> {
                        l.clearChildren();
                        l.left();
                        for(var liquid : Vars.content.liquids()){
                            if(building.liquids.hasFlowLiquid(liquid)){
                                l.image(liquid.uiIcon).scaling(Scaling.fit).size(32f).padRight(3f);
                                l.label(() -> building.liquids.getFlowRate(liquid) < 0 ? "..." : Strings.fixed(building.liquids.getFlowRate(liquid), 1) + ps).color(Color.lightGray);
                                l.row();
                            }
                        }
                    };

                    rebuild.run();
                    l.update(() -> {
                        for(var liquid : Vars.content.liquids()){
                            if(building.liquids.hasFlowLiquid(liquid) && !current.get(liquid.id)){
                                current.set(liquid.id);
                                rebuild.run();
                            }
                        }
                    });
                }).left();
            }
        }

        if(Vars.net.active() && building.lastAccessed != null){
            table.row();
            table.add(Core.bundle.format("lastaccessed", building.lastAccessed)).growX().wrap().left();
        }

        table.marginBottom(-5);
    }
}
