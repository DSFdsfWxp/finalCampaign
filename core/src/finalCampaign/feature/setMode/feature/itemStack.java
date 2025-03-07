package finalCampaign.feature.setMode.feature;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.input.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import finalCampaign.bundle.*;
import finalCampaign.feature.setMode.*;
import finalCampaign.map.*;
import finalCampaign.net.*;
import finalCampaign.patch.*;
import finalCampaign.ui.*;
import finalCampaign.ui.layout.*;
import finalCampaign.util.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.defense.turrets.BaseTurret.*;
import mindustry.world.blocks.defense.turrets.ContinuousTurret.*;
import mindustry.world.blocks.defense.turrets.ItemTurret.*;
import mindustry.world.blocks.defense.turrets.LiquidTurret.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class itemStack extends IFeature {
    public itemStack() {
        category = "content";
        name = "itemStack";
        supportMultiSelect = false;
    }

    public boolean isSupported(Building[] selected) {
        Building building = selected[0];
        return !(building.items == null && building.liquids == null && building.power == null);
    }

    public void buildUI(Building[] selected, Table table, bundleNS bundleNS) {
        Building building = selected[0];
        IFcBuilding fcBuilding = (IFcBuilding) building;
        fakeFinal<Boolean> ammoPriorityForceUpdate = new fakeFinal<>(false);

        if (building instanceof BaseTurretBuild) {
            timer rebuildTimer = new timer();
            table.table().update(t -> {
                if (rebuildTimer.marked() && !ammoPriorityForceUpdate.get()) if (rebuildTimer.sTime() < 3f || ((dragLayout) rebuildTimer.customObject).dragging()) return;
                rebuildTimer.mark();
                ammoPriorityForceUpdate.set(false);

                t.clear();
                t.setBackground(Tex.pane);

                dragLayout layout = new dragLayoutX(0f, 32f);
                rebuildTimer.customObject = layout;
                t.add(layout).expandX().left().padLeft(-9f);
                t.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                        fSetMode.setFlickScrollEnabled(false);
                        return true;
                    }

                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                        fSetMode.setFlickScrollEnabled(true);
                    }
                });

                if (building instanceof ItemTurretBuild itb) {
                    float totalAmount = 0;
                    ObjectMap<Element, ItemEntry> map = new ObjectMap<>();
                    for (AmmoEntry ae : itb.ammo) totalAmount += ae.amount == Short.MAX_VALUE ? 1 : Math.max(ae.amount, 0);
                    for (int i=itb.ammo.size - 1; i>=0; i--) {
                        ItemEntry ie = (ItemEntry) itb.ammo.get(i);
                        if (ie.amount <= 0) continue;
                        Table it = new Table();
                        it.setBackground(Tex.whiteui);
                        it.color.set(ie.item.color.cpy().mul(0.5f));
                        it.table(iit -> iit.image(ie.item.uiIcon).size(32f).minWidth(0f).scaling(Scaling.fit).center()).width(248f * ((ie.amount == Short.MAX_VALUE ? 1 : ie.amount) / totalAmount)).touchable(Touchable.enabled);
                        it.addListener(new dragHandle(it, layout));
                        it.addListener(new HandCursorListener());
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

                if (building instanceof LiquidTurretBuild || building instanceof ContinuousTurretBuild) {
                    Liquid current = building.liquids.current();
                    Liquid[] lst = new Liquid[Vars.content.liquids().size];
                    ObjectMap<Element, Liquid> map = new ObjectMap<>();
                    
                    {
                        lst[0] = current;
                        int i = 1;
                        for (Liquid liquid : Vars.content.liquids()) if (liquid != current) lst[i++] = liquid;
                    }

                    for (Liquid liquid : lst) {
                        Table lt = new Table();
                        float amount = building.liquids.get(liquid);
                        if (amount <= 0f) continue;
                        if (amount == Float.POSITIVE_INFINITY) amount = building.block.liquidCapacity;
                        lt.setBackground(Tex.whiteui);
                        lt.color.set(liquid.color.cpy().mul(0.5f));
                        lt.table(lit -> lit.image(liquid.uiIcon).size(32f).minWidth(0f).scaling(Scaling.fit).center()).width(248f * (amount / (building.block.liquidCapacity * Vars.content.liquids().size)));
                        lt.addListener(new dragHandle(lt, layout));
                        lt.addListener(new HandCursorListener());
                        layout.addChild(lt);
                        map.put(lt, liquid);
                    }

                    layout.indexUpdate(() -> {
                        fcCall.setCurrentLiquid(building, map.get(layout.getChildren().first()));
                    });
                }
            }).width(256f).height(40f).center().padBottom(8f).row();
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
            Seq<UnlockableContent> contentLst = new Seq<>();
            fakeFinal<Boolean> hasPower = new fakeFinal<>(false);
            fakeFinal<Boolean> forceRebuild = new fakeFinal<>(true);

            table.table().update(t -> {
                if (rebuildTimer.sTime() < 1f) return;
                rebuildTimer.mark();

                boolean needRebuild = forceRebuild.get();
                if (building.items != null && !(building instanceof ItemTurretBuild)) {
                    for (Item item : Vars.content.items()) {
                        if (building.items.has(item) != contentLst.contains(item)) {
                            needRebuild = true;
                            break;
                        }
                    }
                }

                if (!needRebuild && building instanceof ItemTurretBuild itb) {
                    Seq<Item> checkLst = Vars.content.items().copy();
                    for (AmmoEntry ae : itb.ammo) {
                        ItemEntry ie = (ItemEntry) ae;
                        boolean has = contentLst.contains(ie.item);
                        if (!has || ie.amount <= 0) {
                            needRebuild = true;
                            break;
                        }
                        checkLst.remove(ie.item);
                    }
                    for (Item item : checkLst) {
                        if (contentLst.contains(item)) {
                            needRebuild = true;
                            break;
                        }
                    }
                }

                if (!needRebuild && building.liquids != null) {
                    for (Liquid liquid : Vars.content.liquids()) {
                        if ((building.liquids.get(liquid) > 0f) != contentLst.contains(liquid)) {
                            needRebuild = true;
                            break;
                        }
                    }
                }

                if (!needRebuild && building.power != null && building.block.consPower != null) {
                    boolean has = (fcBuilding.fcInfinityPower() ? Float.POSITIVE_INFINITY : building.power.status * Math.max(building.block.consPower.capacity, building.block.consPower.usage)) > 0f;
                    if (has != hasPower.get()) needRebuild = true;
                }

                if (!needRebuild) return;
                forceRebuild.set(false);
                ammoPriorityForceUpdate.set(true);

                contentLst.clear();
                hasPower.set(false);
                t.clear();
                boolean sandbox = fcMap.sandbox();
                int count = 0;
                if (building.items != null) {
                    for (int i=0; i<building.items.length(); i++) {
                        if (!building.items.has(i)) continue;
                        Item item = Vars.content.item(i);
                        itemImage image = t.add(new itemImage(item.uiIcon, () -> building.items.get(item), () -> building.items.get(item) == Integer.MAX_VALUE)).padRight(8).tooltip(tt -> {
                            tt.setBackground(Tex.button);
                            tt.left();
                            tt.add(item.localizedName).left().padLeft(8f).padRight(8f).row();
                            tt.add("").color(Color.lightGray).left().minWidth(100f).padLeft(8f).padRight(8f).update(txt -> {
                                int val = building.items.get(item);
                                txt.setText(val == Integer.MAX_VALUE ? "∞" : Integer.toString(val));
                            });
                        }).get();
                        image.clicked(() -> {
                            if (currentItem.get() == item && !col.isCollapsed()) {
                                col.setCollapsed(true);
                                currentItem.set(null);
                                return;
                            }
                            int num = building.items.get(item);
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
                        image.addListener(new HandCursorListener());
                        if (++ count % 5 == 0) t.row();
                        contentLst.add(item);
                    }
                }

                if (building instanceof ItemTurretBuild itb) {
                    for (AmmoEntry ae : itb.ammo) {
                        ItemEntry ie = (ItemEntry) ae;
                        if (ie.amount <= 0) continue;
                        itemImage image = t.add(new itemImage(ie.item.uiIcon, () -> ie.amount, () -> ie.amount == Short.MAX_VALUE)).padRight(8).tooltip(tt -> {
                            tt.setBackground(Tex.button);
                            tt.left();
                            tt.add(ie.item.localizedName).left().padLeft(8f).padRight(8f).row();
                            tt.add("").color(Color.lightGray).left().padLeft(8f).padRight(8f).minWidth(100f).update(txt -> txt.setText(ie.amount == Short.MAX_VALUE ? "∞" : Integer.toString(ie.amount)));
                        }).get();
                        image.clicked(() -> {
                            if (currentItemAmmo.get() == ie.item && !col.isCollapsed()) {
                                col.setCollapsed(true);
                                currentItemAmmo.set(null);
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
                        image.addListener(new HandCursorListener());
                        if (++ count % 5 == 0) t.row();
                        contentLst.add(ie.item);
                    }
                }

                if (building.liquids != null) {
                    for (Liquid liquid : Vars.content.liquids()) {
                        if (building.liquids.get(liquid) <= 0f) continue;
                        itemImage image = t.add(new itemImage(liquid.uiIcon, () -> (int)(building.liquids.get(liquid)), () -> building.liquids.get(liquid) == Float.POSITIVE_INFINITY)).padRight(8f).tooltip(tt -> {
                            tt.setBackground(Tex.button);
                            tt.left();
                            tt.add(liquid.localizedName).left().padLeft(8f).padRight(8f).row();
                            tt.add("").color(Color.lightGray).left().padLeft(8f).padRight(8f).minWidth(100f).update(txt -> {
                                float amount = building.liquids.get(liquid);
                                txt.setText(amount == Float.POSITIVE_INFINITY ? "∞" : Float.toString(amount));
                            });
                        }).get();
                        image.clicked(() -> {
                            if (currentLiquid.get() == liquid && !col.isCollapsed()) {
                                col.setCollapsed(true);
                                currentLiquid.set(null);
                                return;
                            }
                            float amount = building.liquids.get(liquid);
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
                        image.addListener(new HandCursorListener());
                        if (++ count % 5 == 0) t.row();
                        contentLst.add(liquid);
                    }
                }

                if (building.power != null && building.block.consPower != null) {
                    Floatp amount = () -> building.power.status * Math.max(building.block.consPower.capacity, building.block.consPower.usage);
                    if (amount.get() > 0) {
                        itemImage image = t.add(new itemImage(Vars.ui.getIcon(Category.power.name()).getRegion(), () -> (int)(amount.get()), () -> amount.get() == Float.POSITIVE_INFINITY)).padRight(8f).tooltip(tt -> {
                            tt.setBackground(Tex.button);
                            tt.left();
                            tt.add(Core.bundle.get("unit.powerunits")).left().padLeft(8f).padRight(8f).row();
                            tt.add("").color(Color.lightGray).left().padLeft(8f).padRight(8f).minWidth(100f).update(txt -> {
                                float val = amount.get();
                                txt.setText(val == Float.POSITIVE_INFINITY ? "∞" : Float.toString(val));
                            });
                        }).get();
                        image.clicked(() -> {
                            if (currentPower.get() && !col.isCollapsed()) {
                                col.setCollapsed(true);
                                currentPower.set(false);
                                return;
                            }
                            currentItem.set(null);
                            currentItemAmmo.set(null);
                            currentLiquid.set(null);
                            currentPower.set(true);
                            currentAmount.set(amount.get());
                            currentAmountInfinity.set(amount.get() == Float.POSITIVE_INFINITY);
                            rebuildCol.get().run();
                            if (col.isCollapsed()) col.setCollapsed(false);
                            if (!addCol.isCollapsed()) addCol.setCollapsed(true);
                        });
                        image.addListener(new HandCursorListener());
                        if (++ count % 5 == 0) t.row();
                        hasPower.set(true);
                    }
                }

                if (sandbox) {
                    Image image = t.image(Icon.add).size(32f).scaling(Scaling.fit).padRight(8f).get();
                    image.clicked(() -> {
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
                    image.addListener(new HandCursorListener());
                    if (++ count % 5 == 0) t.row();
                }
            }).center().growX().row();

            col.setTable(new Table(ctc -> {
                ctc.setBackground(Tex.pane);

                rebuildCol.set(() -> {
                    ctc.clear();

                    ctc.table(tc -> {
                        fakeFinal<UnlockableContent> selectedContent = new fakeFinal<>();

                        if (currentItem.get() != null) {
                            Item current = currentItem.get();
                            selectedContent.set(current);
                            tc.image(current.uiIcon).size(32f).scaling(Scaling.fit).left().padLeft(12f);
                            tc.add(current.localizedName).left().padLeft(4f).padRight(4f).wrap().growY();
                            tc.add("").right().wrap().grow().labelAlign(Align.right).padRight(12f).update(l -> l.setText(building.items.get(current) == Integer.MAX_VALUE ? "∞" : Integer.toString(building.items.get(current)))).row();
                        }
                        if (currentItemAmmo.get() != null) {
                            Item current = currentItemAmmo.get();
                            selectedContent.set(current);
                            tc.image(current.uiIcon).size(32f).scaling(Scaling.fit).left().padLeft(12f);
                            tc.add(current.localizedName).left().padLeft(4f).wrap().growY();
                            tc.add("").right().wrap().grow().labelAlign(Align.right).padRight(12f).update(l -> {
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
                            tc.image(current.uiIcon).size(32f).scaling(Scaling.fit).left().padLeft(12f);
                            tc.add(current.localizedName).left().padLeft(4f).padRight(4f).wrap().growY();
                            tc.add("").right().wrap().wrap().grow().labelAlign(Align.right).padRight(12f).update(l -> l.setText(building.liquids.get(current) == Float.POSITIVE_INFINITY ? "∞" : Float.toString(building.liquids.get(current)))).row();
                        }
                        if (currentPower.get()) {
                            selectedContent.set(null);
                            tc.image(Vars.ui.getIcon(Category.power.name()).getRegion()).size(32f).scaling(Scaling.fit).left().padLeft(12f);
                            tc.add(Core.bundle.get("unit.powerunits")).left().padLeft(4f).padRight(4f).wrap().growY();
                            if (building.block.consPower != null) tc.add("").right().wrap().wrap().grow().labelAlign(Align.right).padRight(12f).update(l -> l.setText(building.power.status == Float.POSITIVE_INFINITY ? "∞" : Float.toString(building.power.status * Math.max(building.block.consPower.capacity, building.block.consPower.usage))));
                            tc.row();
                        }
    
                        Player player = Vars.player;
                        if (player.dead()) return;
                        Unit unit = player.unit();
                        boolean sandbox = fcMap.sandbox();
                        float capacity = sandbox ? (currentAmountInfinity.get() ? unit.type.itemCapacity : currentAmount.get()) : unit.type.itemCapacity;
    
                        if (capacity <= 0) {
                            tc.add(bundleNS.get("invalidPlayerUnit")).color(Color.lightGray).colspan(3).padTop(8f).center();
                        } else {
                            textSlider slider = tc.add(new textSlider("", 0, 0, capacity, 1f, 256f)).width(256f).padTop(8f).center().colspan(3).get();
                            slider.showNum = false;
                            tc.row();
                            tc.table(butt -> {
                                TextField field = butt.field("0", txt -> slider.rawValue(Integer.parseInt(txt))).valid(Strings::canParsePositiveFloat).growX().minWidth(10f).padRight(4f).padLeft(8f).left().get();
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
                                        float taken = Math.min(building.power.status * Math.max(building.block.consPower.capacity, building.block.consPower.usage), slider.value());
                                        fcCall.takePower(unit, building, taken);
                                    }
                                }).right().width(75f).padRight(4f);
                                butt.button(bundleNS.get("remove"), () -> {
                                    if (selectedContent.get() instanceof Item item) {
                                        if (building instanceof ItemTurretBuild itb) {
                                            int amount = 0;
                                            for (AmmoEntry ae : itb.ammo) {
                                                ItemEntry ie = (ItemEntry) ae;
                                                if (ie.item != item) continue;
                                                amount = ie.amount;
                                                break;
                                            }
                                            int removed = (int) Math.min(amount, slider.value());
                                            if (sandbox) {
                                                fcCall.setTurretAmmo(unit, building, item, amount == Short.MAX_VALUE ? 0 : amount - removed);
                                            } else {
                                                removed = Math.min(removed, unit.type.itemCapacity - unit.stack.amount);
                                                fcCall.setTurretAmmo(unit, building, item, -removed);
                                            }
                                            ammoPriorityForceUpdate.set(true);
                                        } else {
                                            int currentNum = building.items.get(item);
                                            int removed = (int) Math.min(currentNum, slider.value());
                                            if (sandbox) {
                                                fcCall.setItem(unit, building, item, currentNum == Integer.MAX_VALUE ? 0 : currentNum - removed);
                                            } else {
                                                removed = Math.min(removed, unit.type.itemCapacity - unit.stack.amount);
                                                fcCall.setItem(unit, building, item, -removed);
                                            }
                                        }
                                    } else if (selectedContent.get() instanceof Liquid liquid) {
                                        float currentNum = building.liquids.get(liquid);
                                        float removed = Math.min(currentNum, slider.value());
                                        fcCall.setLiquid(building, liquid, currentNum == Float.POSITIVE_INFINITY ? 0 : currentNum - removed);
                                        ammoPriorityForceUpdate.set(true);
                                    } else if (currentPower.get() && building.block.consPower != null) {
                                        float current = building.power.status * Math.max(building.block.consPower.capacity, building.block.consPower.usage);
                                        float removed = Math.min(current, slider.value());
                                        fcCall.setPower(building, current == Float.POSITIVE_INFINITY ? 0 : current - removed);
                                    }
                                }).width(75f).right().padRight(8f).get().setDisabled(() -> !(selectedContent.get() instanceof Item) && !sandbox);
                            }).center().colspan(3).padTop(4f).growX();
                        }
                    }).growX().padTop(8f).padBottom(8f);
                });
            }));

            addCol.setTable(new Table(ctc -> {
                ctc.setBackground(Tex.pane);

                rebuildAddCol.set(() -> {
                    ctc.clear();

                    ctc.table(t -> {
                        contentSelecter selecter = new contentSelecter();
                        fakeFinal<barSetter> setter = new fakeFinal<>();
                        for (Item item : Vars.content.items()) if (building.block.consumesItem(item) || building.acceptItem(building, item)) selecter.add(item);
                        for (Liquid liquid : Vars.content.liquids()) if (building.block.consumesLiquid(liquid) || building.acceptLiquid(building, liquid)) selecter.add(liquid);
                        if (building.power != null && building.block.consPower != null) selecter.add(Vars.ui.getIcon(Category.power.name()), "power");
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
                                    float amount = building.power.status * Math.max(building.block.consPower.capacity, building.block.consPower.usage);
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
                                        if (amount != Short.MAX_VALUE) amount = Math.min(amount, ((ItemTurret) building.block).maxAmmo);
                                        fcCall.setTurretAmmo(unit, building, item, amount);
                                        forceRebuild.set(true);
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
                        selecter.modified(() -> {
                            setterTable.clear();
                            UnlockableContent content = selecter.getSelectedContent();
                            if (content == null) {
                                String name = selecter.getSelectedName();
                                if (name == null) name = "";
                                if (name.equals("power")) {
                                    setter.set(new barSetter("", 218f, Math.max(building.block.consPower.capacity, building.block.consPower.usage), 0, 0, false, true, true, true, true));
                                } else {
                                    setter.set(new barSetter("", 218f, 100f, 0, 0, false, true, true, true, true));
                                    setter.get().setDisabled(true);
                                }
                            } else {
                                if (content instanceof Item) {
                                    setter.set(new barSetter("", 218f, building instanceof ItemTurretBuild ? ((ItemTurret) building.block).maxAmmo : building.block.itemCapacity, 0, 0, true, true, true, true, true));
                                } else if (content instanceof Liquid) {
                                    setter.set(new barSetter("", 218f, building.block.liquidCapacity, 0, 0, false, true, true, true, true));
                                }
                            }

                            setterTable.add(setter.get()).left().width(218f);
                        });
                        selecter.fireModified();
                    }).growX().margin(8f);
                });
            }));

            table.add(col).center().growX().padTop(4f).row();
            table.add(addCol).growX().center();
        }
    }
}
