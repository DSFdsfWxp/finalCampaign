package finalCampaign.feature.featureClass.control.setMode.setFeature;

import arc.*;
import arc.graphics.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import finalCampaign.bundle.*;
import finalCampaign.feature.featureClass.control.setMode.*;
import finalCampaign.net.*;
import finalCampaign.patch.*;
import finalCampaign.ui.*;
import finalCampaign.util.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.BaseTurret.*;
import mindustry.world.blocks.defense.turrets.ItemTurret.*;
import mindustry.world.blocks.defense.turrets.LiquidTurret.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class itemStack extends iFeature {
    public itemStack() {
        category = "content";
        name = "itemStack";
        supportMultiSelect = false;
    }

    public boolean isSupported(Building[] selected) {
        Building building = selected[0];
        if (building instanceof BaseTurretBuild) {
            if (!(building instanceof ItemTurretBuild) && !(building instanceof LiquidTurretBuild) && building.power == null) return false;
        } else {
            if (building.items == null && building.liquids == null && building.power == null) return false;
        }
        return true;
    }

    public void buildUI(Building[] selected, Table table, bundleNS bundleNS) {
        Building building = selected[0];
        IFcBuilding fcBuilding = (IFcBuilding) building;
        timer rebuildTimer = new timer();

        if (building instanceof BaseTurretBuild) {
            fakeFinal<Item> currentItem = new fakeFinal<>();
            Collapser col = new Collapser(new Table(), true);
            fakeFinal<Runnable> rebuildCol = new fakeFinal<>();

            table.table().update(t -> {
                if (rebuildTimer.marked()) if (rebuildTimer.sTime() < 0.2f || ((dragLayout) rebuildTimer.customObject).dragging()) return;
                rebuildTimer.mark();

                t.clear();
                t.setBackground(Tex.pane);
                t.setWidth(Scl.scl(172f));

                dragLayout layout = new dragLayoutX(0f, 22f);
                rebuildTimer.customObject = layout;
                t.add(layout).center().row();

                if (building instanceof ItemTurretBuild itb) {
                    float totalAmount = 0;
                    ObjectMap<Element, ItemEntry> map = new ObjectMap<>();
                    for (AmmoEntry ae : itb.ammo) totalAmount += ae.amount == Integer.MAX_VALUE ? 1 : (ae.amount < 0 ? 0 : ae.amount);
                    for (int i=itb.ammo.size - 1; i>=0; i--) {
                        ItemEntry ie = (ItemEntry) itb.ammo.get(i);
                        if (ie.amount <= 0) continue;
                        Table it = new Table();
                        it.setWidth(t.getWidth() * ((ie.amount == Integer.MAX_VALUE ? totalAmount * 0.1f : ie.amount) / totalAmount));
                        it.setBackground(Tex.whiteui);
                        it.color.set(ie.item.color);
                        it.image(ie.item.uiIcon).size(32f).scaling(Scaling.fit).center().get().addListener(new forwardEventListener(it));
                        it.addListener(new dragHandle(it, layout));
                        it.clicked(() -> {
                            currentItem.set(ie.item);
                            rebuildCol.get().run();
                            col.toggle();
                        });
                        layout.addChild(it);
                        map.put(it, ie);
                    }

                    layout.indexUpdate(() -> {
                        for (ItemEntry ie : map.values()) {
                            for (int i=0; i<itb.ammo.size; i++) {
                                ItemEntry nie = (ItemEntry) itb.ammo.get(i);
                                if (nie.item == ie.item) {
                                    map.put(map.findKey(ie, true), nie);
                                    break;
                                }
                            }
                        }

                        Item[] order = new Item[layout.getChildren().size];
                        for (int i=0; i<order.length; i++) {
                            order[i] = map.get(layout.getChildren().get(i)).item;
                        }
                        fcCall.setTurretAmmoOrder(building, order);
                    });
                }

                if (building instanceof LiquidTurretBuild ilb) {
                    Liquid current = ilb.liquids.current();
                    Liquid[] lst = new Liquid[Vars.content.liquids().size];
                    ObjectMap<Element, Liquid> map = new ObjectMap<>();
                    
                    {
                        lst[0] = current;
                        int i = 1;
                        for (Liquid liquid : Vars.content.liquids()) if (liquid != current) lst[i++] = liquid;
                    }

                    for (Liquid liquid : lst) {
                        Table lt = new Table();
                        float amount = ilb.liquids.get(liquid);
                        if (amount == Float.POSITIVE_INFINITY) amount = building.block.liquidCapacity * 0.1f;
                        lt.setWidth(t.getWidth() * (amount / building.block.liquidCapacity));
                        lt.setBackground(Tex.whiteui);
                        lt.color.set(liquid.color);
                        lt.image(liquid.uiIcon).size(32f).scaling(Scaling.fit).center().get().addListener(new forwardEventListener(lt));
                        lt.addListener(new dragHandle(lt, layout));
                        layout.addChild(lt);
                        map.put(lt, liquid);
                    }

                    layout.indexUpdate(() -> {
                        fcCall.setCurrentLiquid(building, map.get(layout.getChildren().first()));
                    });
                }
            }).center().row();

            col.setTable(new Table(tc -> {
                tc.setWidth(Scl.scl(172f));

                rebuildCol.set(() -> {
                    tc.clear();

                    Item current = currentItem.get();
                    Player player = Vars.player;
                    if (current == null || player == null) return;
                    if (player.dead()) return;
                    Unit unit = player.unit();
                    int capacity = unit.type.itemCapacity;
                    ItemTurretBuild turretBuild = (ItemTurretBuild) building;
                    fakeFinal<AmmoEntry> entry = new fakeFinal<>(); 

                    tc.image(current.uiIcon).size(32f).scaling(Scaling.fit).left();
                    tc.add(current.localizedName).padLeft(4f).width(100f).wrap().growY().left();
                    for (AmmoEntry ae : turretBuild.ammo) {
                        ItemEntry ie = (ItemEntry) ae;
                        if (ie.item != current) continue;
                        entry.set(ae);
                        tc.add(Integer.toString(ie.amount)).update(l -> l.setText(Integer.toString(ie.amount))).width(32f).wrap().growY().right();
                        break;
                    }
                    tc.row();

                    if (capacity <= 0) {
                        tc.add(bundleNS.get("invalidPlayerUnit")).color(Color.lightGray).colspan(3).padTop(8f).center();
                    } else {
                        textSlider slider = tc.add(new textSlider(bundleNS.get("num"), 0, 0, capacity, 1f, 172f)).padTop(8f).colspan(3).center().get();
                        tc.row();
                        tc.table(butt -> {
                            TextField field = butt.field("0", txt -> slider.value(Integer.parseInt(txt))).valid(value -> Strings.canParsePositiveInt(value)).width(50f).left().get();
                            slider.changed(() -> field.setText(Float.toString(slider.value())));
                            butt.button(bundleNS.get("take"), () -> {
                                int taken = (int) Math.min(entry.get().amount, slider.value());
                                taken = Math.min(taken, unit.maxAccepted(current));
                                Call.requestItem(player, building, current, taken);
                            }).left().width(50f).padLeft(5f);
                            butt.button(bundleNS.get("remove"), () -> {
                                int removed = (int) Math.min(entry.get().amount, slider.value());
                                removed = Math.min(removed, unit.itemCapacity() - unit.stack.amount);
                                fcCall.setTurretAmmo(unit, building, current, -removed);
                            }).padLeft(5f).width(50f).left();
                        }).center().colspan(3).padTop(4f);
                    }
                });

                rebuildCol.get().run();
            }));

            table.add(col).center().row();
        }

        {
            fakeFinal<Item> currentItem = new fakeFinal<>();
            fakeFinal<Liquid> currentLiquid = new fakeFinal<>();
            fakeFinal<Boolean> currentPower = new fakeFinal<>();
            Collapser col = new Collapser(new Table(), true);
            fakeFinal<Runnable> rebuildCol = new fakeFinal<>();

            table.table().update(t -> {
                if (rebuildTimer.sTime() < 0.2f) return;
                rebuildTimer.mark();

                t.clear();
                int count = 0;
                if (building.items != null) {
                    for (int i=0; i<building.items.length(); i++) {
                        if (!building.items.has(i)) continue;
                        int num = building.items.get(i);
                        Item item = Vars.content.item(i);
                        t.add(new ItemImage(item.uiIcon, num)).padRight(8).tooltip(tt -> {
                            tt.add(item.localizedName).left().row();
                            tt.add(num == Integer.MAX_VALUE ? "∞" : Integer.toString(num)).color(Color.lightGray).left();
                        }).get().clicked(() -> {
                            currentItem.set(item);
                            currentLiquid.set(null);
                            currentPower.set(false);
                            rebuildCol.get().run();
                            col.toggle();
                        });
                        if (++ count % 5 == 0) t.row();
                    }
                }

                if (building.liquids != null) {
                    for (Liquid liquid : Vars.content.liquids()) {
                        float amount = building.liquids.get(liquid);
                        if (amount <= 0f) continue;
                        t.add(new ItemImage(liquid.uiIcon, amount == Float.POSITIVE_INFINITY ? Integer.MAX_VALUE : (int) amount)).padRight(8f).tooltip(tt -> {
                            tt.add(liquid.localizedName).left().row();
                            tt.add(amount == Float.POSITIVE_INFINITY ? "∞" : Float.toString(amount)).color(Color.lightGray).left();
                        }).get().clicked(() -> {
                            currentItem.set(null);
                            currentLiquid.set(liquid);
                            currentPower.set(false);
                            rebuildCol.get().run();
                            col.toggle();
                        });
                        if (++ count % 5 == 0) t.row();
                    }
                }

                if (building.power != null) {
                    float amount = fcBuilding.fcInfinityPower() ? Float.POSITIVE_INFINITY : building.power.status * building.block.consPower.capacity;
                    if (amount != Float.NaN && amount > 0) {
                        t.add(new ItemImage(Vars.ui.getIcon(Category.power.name()).getRegion(), amount == Float.POSITIVE_INFINITY ? Integer.MAX_VALUE : (int) amount)).padRight(8f).color(Pal.accent).tooltip(tt -> {
                            tt.add(Core.bundle.get("unit.powerunits")).row();
                            tt.add(amount == Float.POSITIVE_INFINITY ? "∞" : Float.toString(amount)).color(Color.lightGray).left();
                        }).get().clicked(() -> {
                            currentItem.set(null);
                            currentLiquid.set(null);
                            currentPower.set(true);
                            rebuildCol.get().run();
                            col.toggle();
                        });
                        if (++ count % 5 == 0) t.row();
                    }
                }
            }).center();

            col.setTable(new Table(tc -> {
                tc.setWidth(Scl.scl(172f));

                rebuildCol.set(() -> {
                    tc.clear();

                    fakeFinal<UnlockableContent> selectedContent = new fakeFinal<>();

                    if (currentItem.get() != null) {
                        Item current = currentItem.get();
                        selectedContent.set(current);
                        tc.image(current.uiIcon).size(32f).scaling(Scaling.fit).left();
                        tc.add(current.localizedName).left().padLeft(4f).width(100f).wrap().growY();
                        tc.add("").right().width(32f).wrap().growY().update(l -> l.setText(Integer.toString(building.items.get(current)))).row();
                    }
                    if (currentLiquid.get() != null) {
                        Liquid current = currentLiquid.get();
                        selectedContent.set(current);
                        tc.image(current.uiIcon).size(32f).scaling(Scaling.fit).left();
                        tc.add(current.localizedName).left().padLeft(4f).width(100f).wrap().growY();
                        tc.add("").right().width(32f).wrap().growY().update(l -> l.setText(Float.toString(building.liquids.get(current)))).row();
                    }
                    if (currentPower.get()) {
                        selectedContent.set(null);
                        tc.image(Vars.ui.getIcon(Category.power.name()).getRegion()).color(Pal.accent).size(32f).scaling(Scaling.fit).left();
                        tc.add(Core.bundle.get("unit.powerunits")).left().padLeft(4f).width(100f).wrap().growY();
                        if (building.block.consPower != null) tc.add("").right().width(32f).wrap().growY().update(l -> l.setText(Float.toString(building.power.status * building.block.consPower.capacity)));
                        tc.row();
                    }

                    Player player = Vars.player;
                    if (player.dead()) return;
                    Unit unit = player.unit();
                    int capacity = unit.type.itemCapacity;

                    if (capacity <= 0) {
                        tc.add(bundleNS.get("invalidPlayerUnit")).color(Color.lightGray).colspan(3).padTop(8f).center();
                    } else {
                        textSlider slider = tc.add(new textSlider(bundleNS.get("num"), 0, 0, capacity, 1f, 172f)).width(172f).padTop(8f).center().colspan(3).get();
                        tc.row();
                        tc.table(butt -> {
                            TextField field = butt.field("0", txt -> slider.value(Integer.parseInt(txt))).valid(value -> Strings.canParsePositiveInt(value)).width(50f).left().get();
                            slider.changed(() -> field.setText(Float.toString(slider.value())));
                            butt.button(bundleNS.get("take"), () -> {
                                if (selectedContent.get() instanceof Item item) {
                                    int taken = (int) Math.min(building.items.get(item), slider.value());
                                    taken = Math.min(taken, unit.maxAccepted(item));
                                    Call.requestItem(player, building, item, taken);
                                } else if (selectedContent.get() instanceof Liquid liquid) {
                                    float taken = Math.min(building.liquids.get(liquid), capacity);
                                    fcCall.takeLiquid(unit, building, liquid, taken);
                                } else if (currentPower.get() && building.block.consPower != null) {
                                    float taken = Math.min(building.power.status * building.block.consPower.capacity, capacity);
                                    fcCall.takePower(unit, building, taken);
                                }
                            }).left().width(50f).padLeft(5f);
                            butt.button(bundleNS.get("remove"), () -> {
                                if (selectedContent.get() instanceof Item item) {
                                    int removed = (int) Math.min(building.items.get(item), slider.value());
                                    removed = Math.min(removed, unit.stack.amount);
                                    fcCall.setItem(unit, building, item, -removed);
                                }
                            }).padLeft(5f).width(50f).left().get().setDisabled(() -> !(selectedContent.get() instanceof Item));
                        }).center().colspan(3).padTop(4f);
                    }
                });

                rebuildCol.get().run();
            }));

            table.add(col).center();
        }
    }
}
