package finalCampaign.feature.featureClass.control.setMode.setFeature;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import finalCampaign.bundle.*;
import finalCampaign.feature.featureClass.control.setMode.*;
import finalCampaign.net.*;
import finalCampaign.patch.*;
import finalCampaign.ui.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.heat.*;
import mindustry.world.meta.*;

public class basicInfo extends IFeature {
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
        IFcBlock block = (IFcBlock) building.block;

        table.table(bars -> {
            OrderedMap<String, Func<Building, Bar>> map = block.fcBarMap();
            for (String key : map.keys()) {
                Bar bar = map.get(key).get(building);
                if (bar == null) continue;
                bars.add(bar).growX().height(18f).pad(4);

                boolean consHeat = false;
                building.block.checkStats();
                var catMap = building.block.stats.toMap().get(StatCat.crafting);
                if (catMap != null) {
                    var m = catMap.get(Stat.input);
                    if (m != null) for (StatValue v : m) if (v instanceof IFcStatNumberValue numVal) if (numVal.unit() == StatUnit.heatUnits) consHeat = true;
                }
                
                if (key.equals("health") && Vars.state.rules.mode() == Gamemode.sandbox) {
                    barSetter setter = new barSetter(bundleNS.get("health"), 264f, building.maxHealth, 0, building.health, false, true, false, true, true);
                    Collapser col = new Collapser(new Table(ct -> {
                        ct.setBackground(Tex.sliderBack);
                        ct.fillParent = true;
                        ct.add(setter).pad(4f);
                    }), true);

                    setter.modified(() -> {
                        fcCall.setHealth(building, setter.value());
                    });

                    bar.hovered(() -> bar.outline(Pal.accent, 2f));
                    bar.exited(() -> {
                        if (col.isCollapsed()) bar.outline(Pal.accent, 0);
                    });
                    bar.clicked(() -> {
                        col.toggle();
                        bar.outline(Pal.accent, col.isCollapsed() ? 0f : 2f);
                    });
                    bar.addListener(new HandCursorListener());
                    bars.row();
                    bars.add(col).growX().center();
                } else if (key.equals("heat") && Vars.state.rules.mode() == Gamemode.sandbox && (building instanceof HeatConsumer || (building.block instanceof Turret tb &&  tb.heatRequirement > 0f) || consHeat)) {

                    // wait for design

                    bar.hovered(() -> bar.outline(Pal.accent, 2f));
                    bar.exited(() -> bar.outline(Pal.accent, 0));
                    bar.addListener(new HandCursorListener());

                    bars.row();
                }
                
                bars.row();
            }
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
