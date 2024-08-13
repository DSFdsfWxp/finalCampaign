package finalCampaign.feature.featureClass.control.setMode.setFeature;

import arc.*;
import arc.graphics.*;
import arc.scene.*;
import arc.scene.event.*;
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
import mindustry.game.*;
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

        if (building instanceof BaseTurretBuild) {
            timer rebuildTimer = new timer();
            table.table().update(t -> {
                if (rebuildTimer.marked()) if (rebuildTimer.sTime() < 0.2f || ((dragLayout) rebuildTimer.customObject).dragging()) return;
                rebuildTimer.mark();

                t.clear();
                t.setBackground(Tex.pane);
                t.left();

                dragLayout layout = new dragLayoutX(0f, 22f);
                rebuildTimer.customObject = layout;
                t.add(layout).center().row();

                if (building instanceof ItemTurretBuild itb) {
                    float totalAmount = 0;
                    ObjectMap<Element, ItemEntry> map = new ObjectMap<>();
                    for (AmmoEntry ae : itb.ammo) totalAmount += ae.amount == Short.MAX_VALUE ? 1 : (ae.amount < 0 ? 0 : ae.amount);
                    for (int i=itb.ammo.size - 1; i>=0; i--) {
                        ItemEntry ie = (ItemEntry) itb.ammo.get(i);
                        if (ie.amount <= 0) continue;
                        Table it = new Table();
                        it.setWidth(256f * ((ie.amount == Short.MAX_VALUE ? totalAmount * 0.1f : ie.amount) / totalAmount));
                        it.setBackground(Tex.whiteui);
                        it.color.set(ie.item.color);
                        it.image(ie.item.uiIcon).size(32f).scaling(Scaling.fit).center().get();
                        it.touchable = Touchable.disabled;
                        it.addListener(new dragHandle(it, layout));
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
                        lt.setWidth(256f * (amount / building.block.liquidCapacity));
                        lt.setBackground(Tex.whiteui);
                        lt.color.set(liquid.color);
                        lt.image(liquid.uiIcon).size(32f).scaling(Scaling.fit).center().get();
                        lt.touchable = Touchable.disabled;
                        lt.addListener(new dragHandle(lt, layout));
                        layout.addChild(lt);
                        map.put(lt, liquid);
                    }

                    layout.indexUpdate(() -> {
                        fcCall.setCurrentLiquid(building, map.get(layout.getChildren().first()));
                    });
                }
            }).width(256f).center().row();
        }

        {
            timer rebuildTimer = new timer();
            fakeFinal<Item> currentItem = new fakeFinal<>();
            fakeFinal<Item> currentItemAmmo = new fakeFinal<>();
            fakeFinal<Liquid> currentLiquid = new fakeFinal<>();
            fakeFinal<Boolean> currentPower = new fakeFinal<>(false);
            Collapser col = new Collapser(new Table(), true);
            Collapser addCol = new Collapser(new Table(), true);
            fakeFinal<Runnable> rebuildCol = new fakeFinal<>();
            fakeFinal<Runnable> rebuildAddCol = new fakeFinal<>();
            fakeFinal<Float> currentAmount = new fakeFinal<>(0f);
            fakeFinal<Boolean> currentAmountInfinity = new fakeFinal<>(false);

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
                            tt.setBackground(Tex.button);
                            tt.add(item.localizedName).left().row();
                            tt.add(num == Integer.MAX_VALUE ? "∞" : Integer.toString(num)).color(Color.lightGray).left();
                        }).get().clicked(() -> {
                            if (currentItem.get() == item) {
                                col.setCollapsed(true);
                                return;
                            }
                            currentItem.set(item);
                            currentItemAmmo.set(null);
                            currentLiquid.set(null);
                            currentPower.set(false);
                            currentAmount.set((float) num);
                            currentAmountInfinity.set(num == Integer.MAX_VALUE);
                            rebuildCol.get().run();
                            if (col.isCollapsed()) col.setCollapsed(false);
                            if (!addCol.isCollapsed()) addCol.setCollapsed(true);
                        });
                        if (++ count % 5 == 0) t.row();
                    }
                }

                if (building instanceof ItemTurretBuild itb) {
                    for (AmmoEntry ae : itb.ammo) {
                        ItemEntry ie = (ItemEntry) ae;
                        t.add(new ItemImage(ie.item.uiIcon, ie.amount)).padRight(8).tooltip(tt -> {
                            tt.setBackground(Tex.button);
                            tt.add(ie.item.localizedName).left().row();
                            tt.add(ie.amount == Short.MAX_VALUE ? "∞" : Integer.toString(ie.amount)).color(Color.lightGray).left();
                        }).get().clicked(() -> {
                            if (currentItem.get() == ie.item) {
                                col.setCollapsed(true);
                                return;
                            }
                            currentItem.set(null);
                            currentItemAmmo.set(ie.item);
                            currentLiquid.set(null);
                            currentPower.set(false);
                            currentAmount.set((float) ie.amount);
                            currentAmountInfinity.set(ie.amount == Integer.MAX_VALUE);
                            rebuildCol.get().run();
                            if (col.isCollapsed()) col.setCollapsed(false);
                            if (!addCol.isCollapsed()) addCol.setCollapsed(true);
                        });
                        if (++ count % 5 == 0) t.row();
                    }
                }

                if (building.liquids != null) {
                    for (Liquid liquid : Vars.content.liquids()) {
                        float amount = building.liquids.get(liquid);
                        if (amount <= 0f) continue;
                        t.add(new ItemImage(liquid.uiIcon, amount == Float.POSITIVE_INFINITY ? Integer.MAX_VALUE : (int) amount)).padRight(8f).tooltip(tt -> {
                            tt.setBackground(Tex.button);
                            tt.add(liquid.localizedName).left().row();
                            tt.add(amount == Float.POSITIVE_INFINITY ? "∞" : Float.toString(amount)).color(Color.lightGray).left();
                        }).get().clicked(() -> {
                            if (currentLiquid.get() == liquid) {
                                col.setCollapsed(true);
                                return;
                            }
                            currentItem.set(null);
                            currentItemAmmo.set(null);
                            currentLiquid.set(liquid);
                            currentPower.set(false);
                            currentAmount.set(amount);
                            currentAmountInfinity.set(amount == Float.POSITIVE_INFINITY);
                            rebuildCol.get().run();
                            if (col.isCollapsed()) col.setCollapsed(false);
                            if (!addCol.isCollapsed()) addCol.setCollapsed(true);
                        });
                        if (++ count % 5 == 0) t.row();
                    }
                }

                if (building.power != null) {
                    float amount = fcBuilding.fcInfinityPower() ? Float.POSITIVE_INFINITY : building.power.status * building.block.consPower.capacity;
                    if (amount != Float.NaN && amount > 0) {
                        t.add(new ItemImage(Vars.ui.getIcon(Category.power.name()).getRegion(), amount == Float.POSITIVE_INFINITY ? Integer.MAX_VALUE : (int) amount)).padRight(8f).color(Pal.accent).tooltip(tt -> {
                            tt.setBackground(Tex.button);
                            tt.add(Core.bundle.get("unit.powerunits")).row();
                            tt.add(amount == Float.POSITIVE_INFINITY ? "∞" : Float.toString(amount)).color(Color.lightGray).left();
                        }).get().clicked(() -> {
                            if (currentPower.get()) {
                                col.setCollapsed(true);
                                return;
                            }
                            currentItem.set(null);
                            currentItemAmmo.set(null);
                            currentLiquid.set(null);
                            currentPower.set(true);
                            currentAmount.set(amount);
                            currentAmountInfinity.set(amount == Float.POSITIVE_INFINITY);
                            rebuildCol.get().run();
                            if (col.isCollapsed()) col.setCollapsed(false);
                            if (!addCol.isCollapsed()) addCol.setCollapsed(true);
                        });
                        if (++ count % 5 == 0) t.row();
                    }
                }

                if (Vars.state.rules.mode() == Gamemode.sandbox) {
                    t.image(Icon.add).size(32f).scaling(Scaling.fit).padRight(8f).get().clicked(() -> {
                        currentItem.set(null);
                        currentItemAmmo.set(null);
                        currentLiquid.set(null);
                        currentPower.set(false);
                        currentAmount.set(0f);
                        currentAmountInfinity.set(false);
                        if (!col.isCollapsed()) col.setCollapsed(true);
                        addCol.toggle();
                        if (!addCol.isCollapsed()) rebuildAddCol.get().run();
                    });
                    if (++ count % 5 == 0) t.row();
                }
            }).center().growX().row();

            col.setTable(new Table(ctc -> {
                ctc.setBackground(Tex.sliderBack);

                rebuildCol.set(() -> {
                    ctc.clear();

                    ctc.table(tc -> {
                        fakeFinal<UnlockableContent> selectedContent = new fakeFinal<>();

                        if (currentItem.get() != null) {
                            Item current = currentItem.get();
                            selectedContent.set(current);
                            tc.image(current.uiIcon).size(32f).scaling(Scaling.fit).left().padLeft(16f);
                            tc.add(current.localizedName).left().padLeft(4f).padRight(4f).wrap().growY();
                            tc.add("").right().wrap().grow().labelAlign(Align.right).padRight(16f).update(l -> l.setText(building.items.get(current) == Integer.MAX_VALUE ? "∞" : Integer.toString(building.items.get(current)))).row();
                        }
                        if (currentItemAmmo.get() != null) {
                            Item current = currentItemAmmo.get();
                            selectedContent.set(current);
                            tc.image(current.uiIcon).size(32f).scaling(Scaling.fit).left().padLeft(16f);
                            tc.add(current.localizedName).left().padLeft(4f).wrap().growY();
                            tc.add("").right().wrap().grow().labelAlign(Align.right).padRight(16f).update(l -> {
                                String txt = "N/A";
                                if (building instanceof ItemTurretBuild itb) {
                                    for (AmmoEntry ae : itb.ammo) {
                                        ItemEntry ie = (ItemEntry) ae;
                                        if (ie.item != current) continue;
                                        txt = ie.amount == Short.MAX_VALUE ? "∞" : Integer.toString(ie.amount);
                                        break;
                                    }
                                }
                                l.setText(txt);
                            }).row();
                        }
                        if (currentLiquid.get() != null) {
                            Liquid current = currentLiquid.get();
                            selectedContent.set(current);
                            tc.image(current.uiIcon).size(32f).scaling(Scaling.fit).left().padLeft(16f);
                            tc.add(current.localizedName).left().padLeft(4f).padRight(4f).wrap().growY();
                            tc.add("").right().wrap().wrap().grow().labelAlign(Align.right).padRight(16f).update(l -> l.setText(building.liquids.get(current) == Float.POSITIVE_INFINITY ? "∞" : Float.toString(building.liquids.get(current)))).row();
                        }
                        if (currentPower.get()) {
                            selectedContent.set(null);
                            tc.image(Vars.ui.getIcon(Category.power.name()).getRegion()).size(32f).scaling(Scaling.fit).left().padLeft(16f);
                            tc.add(Core.bundle.get("unit.powerunits")).left().padLeft(4f).padRight(4f).wrap().growY();
                            if (building.block.consPower != null) tc.add("").right().wrap().wrap().grow().labelAlign(Align.right).padRight(16f).update(l -> l.setText(building.power.status == Float.POSITIVE_INFINITY ? "∞" : Float.toString(building.power.status * building.block.consPower.capacity)));
                            tc.row();
                        }
    
                        Player player = Vars.player;
                        if (player.dead()) return;
                        Unit unit = player.unit();
                        boolean sandbox = Vars.state.rules.mode() == Gamemode.sandbox;
                        float capacity = sandbox ? (currentAmountInfinity.get() ? unit.type.itemCapacity : currentAmount.get()) : unit.type.itemCapacity;
    
                        if (capacity <= 0) {
                            tc.add(bundleNS.get("invalidPlayerUnit")).color(Color.lightGray).colspan(3).padTop(8f).center();
                        } else {
                            textSlider slider = tc.add(new textSlider("", 0, 0, capacity, 1f, 256f)).width(256f).padTop(8f).center().colspan(3).get();
                            slider.showNum = false;
                            tc.row();
                            tc.table(butt -> {
                                TextField field = butt.field("0", txt -> slider.rawValue(Integer.parseInt(txt))).valid(value -> Strings.canParsePositiveFloat(value)).growX().minWidth(10f).padRight(4f).padLeft(16f).left().get();
                                slider.modified(() -> field.setText(selectedContent.get() instanceof Item ? Integer.toString((int) slider.value()) : Float.toString(slider.value())));
                                butt.button(bundleNS.get("take"), () -> {
                                    if (selectedContent.get() instanceof Item item) {
                                        if (building instanceof ItemTurretBuild itb) {
                                            int amount = 0;
                                            for (AmmoEntry ae : itb.ammo) {
                                                ItemEntry ie = (ItemEntry) ae;
                                                if (ie.item != item) continue;
                                                amount = ie.amount;
                                                break;
                                            }
                                            int taken = (int) Math.min(amount, slider.value());
                                            taken = Math.min(taken, unit.maxAccepted(item));
                                            fcCall.takeTurretAmmo(unit, building, item, taken);
                                        } else {
                                            int taken = (int) Math.min(building.items.get(item), slider.value());
                                            taken = Math.min(taken, unit.maxAccepted(item));
                                            Call.requestItem(player, building, item, taken);
                                        }
                                    } else if (selectedContent.get() instanceof Liquid liquid) {
                                        float taken = Math.min(building.liquids.get(liquid), slider.value());
                                        fcCall.takeLiquid(unit, building, liquid, taken);
                                    } else if (currentPower.get() && building.block.consPower != null) {
                                        float taken = Math.min(building.power.status * building.block.consPower.capacity, slider.value());
                                        fcCall.takePower(unit, building, taken);
                                    }
                                }).right().width(75f).padRight(4f);
                                butt.button(bundleNS.get("remove"), () -> {
                                    if (selectedContent.get() instanceof Item item && !currentAmountInfinity.get()) {
                                        if (building instanceof ItemTurretBuild itb) {
                                            int amount = 0;
                                            for (AmmoEntry ae : itb.ammo) {
                                                ItemEntry ie = (ItemEntry) ae;
                                                if (ie.item != item) continue;
                                                amount = ie.amount;
                                                break;
                                            }
                                            int removed = (int) Math.min(amount, slider.value());
                                            removed = Math.min(removed, unit.stack.amount);
                                            if (sandbox) {
                                                fcCall.setTurretAmmo(unit, building, item, amount - removed);
                                            } else {
                                                fcCall.setTurretAmmo(unit, building, item, -removed);
                                            }
                                        } else {
                                            int removed = (int) Math.min(building.items.get(item), slider.value());
                                            removed = Math.min(removed, unit.stack.amount);
                                            if (sandbox) {
                                                fcCall.setItem(unit, building, item, building.items.get(item) - removed);
                                            } else {
                                                fcCall.setItem(unit, building, item, -removed);
                                            }
                                        }
                                    } else if (selectedContent.get() instanceof Liquid liquid && !currentAmountInfinity.get()) {
                                        float removed = Math.min(building.liquids.get(liquid), slider.value());
                                        fcCall.setLiquid(building, liquid, building.liquids.get(liquid) - removed);
                                    } else if (currentPower.get() && building.block.consPower != null && !currentAmountInfinity.get()) {
                                        float current = building.power.status * building.block.consPower.capacity;
                                        float removed = Math.min(current, slider.value());
                                        fcCall.setPower(building, current - removed);
                                    }
                                }).width(75f).right().padRight(16f).get().setDisabled(() -> !(selectedContent.get() instanceof Item) && !sandbox);
                            }).center().colspan(3).padTop(4f).growX();
                        }
                    }).growX().padTop(8f).padBottom(8f);
                });
            }));

            addCol.setTable(new Table(ctc -> {
                ctc.setBackground(Tex.sliderBack);

                rebuildAddCol.set(() -> {
                    ctc.clear();

                    ctc.table(t -> {
                        contentSelecter selecter = new contentSelecter();
                        fakeFinal<barSetter> setter = new fakeFinal<>();
                        for (Item item : Vars.content.items()) if (building.block.consumesItem(item)) selecter.add(item);
                        for (Liquid liquid : Vars.content.liquids()) if (building.block.consumesLiquid(liquid)) selecter.add(liquid);
                        if (building.power != null && building.block.consPower != null) selecter.add(Vars.ui.getIcon(Category.power.name()), "power").color(Pal.accent);
                        t.add(selecter).center().colspan(2).width(240f).row();
                        Table setterTable = t.table().left().padLeft(8f).growX().get();
                        t.button("+", () -> {
                            Player player = Vars.player;
                            if (player.dead()) return;
                            Unit unit = player.unit();

                            UnlockableContent content = selecter.getSelectedContent();
                            if (content == null) {
                                String name = selecter.getSelectedName();
                                if (name == null) name = "";
                                if (name.equals("power")) {
                                    float amount = building.power.status * building.block.consPower.capacity;
                                    fcCall.setPower(building, amount + setter.get().value());
                                }
                            } else {
                                if (content instanceof Item item) {
                                    if (building instanceof ItemTurretBuild itb) {
                                        int amount = 0;
                                        for (AmmoEntry ae : itb.ammo) {
                                            ItemEntry ie = (ItemEntry) ae;
                                            if (ie.item != item) continue;
                                            amount = ie.amount;
                                            break;
                                        }
                                        if (amount == Short.MAX_VALUE) return;
                                        int add = (int) setter.get().value();
                                        amount = add == Integer.MAX_VALUE ? Short.MAX_VALUE : amount + add;
                                        if (amount != Short.MAX_VALUE) amount = Math.min(amount, building.block.itemCapacity);
                                        fcCall.setTurretAmmo(unit, building, item, amount);
                                    } else {
                                        int amount = building.items.get(item);
                                        if (amount == Integer.MAX_VALUE) return;
                                        int add = (int) setter.get().value();
                                        amount = add == Integer.MAX_VALUE ? add : amount + add;
                                        if (amount != Integer.MAX_VALUE) amount = Math.min(amount, building.block.itemCapacity);
                                        fcCall.setItem(unit, building, item, amount);
                                    }
                                } else if (content instanceof Liquid liquid) {
                                    float amount = building.liquids.get(liquid);
                                    fcCall.setLiquid(building, liquid, amount + setter.get().value());
                                }
                            }
                        }).right().width(44f).padLeft(4f).growY().padRight(8f);
                        selecter.changed(() -> {
                            setterTable.clear();
                            UnlockableContent content = selecter.getSelectedContent();
                            if (content == null) {
                                String name = selecter.getSelectedName();
                                if (name == null) name = "";
                                if (name.equals("power")) {
                                    setter.set(new barSetter("", 218f, building.block.consPower.capacity, 0, 0, false, true, true, true, true));
                                } else {
                                    setter.set(new barSetter("", 218f, 100f, 0, 0, false, true, true, true, true));
                                    setter.get().setDisabled(true);
                                }
                            } else {
                                if (content instanceof Item) {
                                    setter.set(new barSetter("", 218f, building.block.itemCapacity, 0, 0, true, true, true, true, true));
                                } else if (content instanceof Liquid) {
                                    setter.set(new barSetter("", 218f, building.block.liquidCapacity, 0, 0, false, true, true, true, true));
                                }
                            }

                            setterTable.add(setter.get()).left().width(218f);
                        });
                        selecter.change();
                    }).growX().margin(8f);
                });
            }));

            table.add(col).center().growX().row();
            table.add(addCol).growX().center();
        }
    }
}
