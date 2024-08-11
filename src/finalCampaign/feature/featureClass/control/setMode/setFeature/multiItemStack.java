package finalCampaign.feature.featureClass.control.setMode.setFeature;

import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import finalCampaign.bundle.*;
import finalCampaign.feature.featureClass.control.setMode.*;
import finalCampaign.net.*;
import finalCampaign.ui.*;
import finalCampaign.util.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.turrets.ItemTurret.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class multiItemStack extends iFeature {
    public multiItemStack() {
        category = "content";
        name = "multiItemStack";
        supportMultiSelect = true;
    }

    public boolean isSupported(Building[] selected) {
        return selected.length > 1 && Vars.state.rules.mode() == Gamemode.sandbox;
    }

    public void buildUI(Building[] selected, Table table, bundleNS bundleNS) {
        ButtonGroup<TextButton> group = new ButtonGroup<>();
        group.setMinCheckCount(0);
        Collapser col = new Collapser(new Table(ct -> {
            ct.setBackground(Tex.sliderBack);
            ct.setWidth(Scl.scl(172f));

            ct.table(t -> {
                contentSelecter selecter = new contentSelecter();
                for (Item item : Vars.content.items()) selecter.add(item);
                for (Liquid liquid : Vars.content.liquids()) selecter.add(liquid);
                selecter.add(Vars.ui.getIcon(Category.power.name()), "power");

                t.add(selecter).width(164f).center().colspan(2).row();
                fakeFinal<barSetter> setter = new fakeFinal<>();
                Table setterTable = t.table().width(120f).left().get();
                t.button("+", () -> {
                    Player player = Vars.player;
                    if (player.dead()) return;
                    Unit unit = player.unit();

                    UnlockableContent content = selecter.getSelectedContent();
                    if (content == null) {
                        String name = selecter.getSelectedName();
                        if (name == null) name = "";
                        if (name.equals("power")) {
                            for (Building building : selected) {
                                if (building.power == null || building.block.consPower == null) continue;
                                float amount = building.power.status * building.block.consPower.capacity + setter.get().value();
                                if (amount != Float.POSITIVE_INFINITY) amount = Math.min(amount, building.block.consPower.capacity);
                                fcCall.setPower(building, amount);
                            }
                        }
                    } else {
                        if (content instanceof Item item) {
                            for (Building building : selected) {
                                if (!building.block.consumesItem(item)) continue;
                                if (building instanceof ItemTurretBuild itb) {
                                    int amount = 0;
                                    for (AmmoEntry ae : itb.ammo) {
                                        ItemEntry ie = (ItemEntry) ae;
                                        if (ie.item != item) continue;
                                        amount = ie.amount;
                                        break;
                                    }
                                    if (amount == Integer.MAX_VALUE) continue;
                                    int add = (int) setter.get().value();
                                    amount = add == Integer.MAX_VALUE ? add : amount + add;
                                    if (amount != Integer.MAX_VALUE) amount = Math.min(amount, building.block.itemCapacity);
                                    fcCall.setTurretAmmo(unit, building, item, amount);
                                } else {
                                    if (building.items == null) continue;
                                    int amount = building.items.get(item);
                                    if (amount == Integer.MAX_VALUE) continue;
                                    int add = (int) setter.get().value();
                                    amount = add == Integer.MAX_VALUE ? add : amount + add;
                                    if (amount != Integer.MAX_VALUE) amount = Math.min(amount, amount);
                                    fcCall.setItem(unit, building, item, amount + (int) setter.get().value());
                                }
                            }
                        } else if (content instanceof Liquid liquid) {
                            for (Building building : selected) {
                                if (!building.block.consumesLiquid(liquid)) continue;
                                if (building.liquids == null) continue;
                                float amount = building.liquids.get(liquid) + setter.get().value();
                                if (amount != Float.POSITIVE_INFINITY) amount = Float.min(amount, building.block.liquidCapacity);
                                fcCall.setLiquid(building, liquid, amount);
                            }
                        }
                    }
                }).width(44f).right();
                selecter.changed(() -> {
                    setterTable.clear();

                    UnlockableContent content = selecter.getSelectedContent();
                    if (content == null) {
                        String name = selecter.getSelectedName();
                        if (name == null) name = "";
                        if (name.equals("power")) {
                            setter.set(new barSetter("", 112f, 1e7f, 0, 0, false, true, true, true, true));
                        } else {
                            setter.set(new barSetter("", 112f, 100f, 0, 0, false, true, true, true, true));
                            setter.get().setDisabled(true);
                        }
                    } else {
                        if (content instanceof Item) {
                            setter.set(new barSetter("", 112f, 1e7f, 0, 0, true, true, true, true, true));
                        } else if (content instanceof Liquid) {
                            setter.set(new barSetter("", 112f, 1e7f, 0, 0, false, true, true, true, true));
                        }
                    }

                    setterTable.add(setter.get()).center();
                });
                selecter.change();
            }).pad(4f);
        }), true);

        table.add(bundleNS.get("removeAll")).width(100f).left();
        table.button(bundleNS.get("remove"), () -> {
            Player player = Vars.player;
            if (player.dead()) return;
            Unit unit = player.unit();

            for (Building building : selected) {
                if (building.items != null) {
                    if (building instanceof ItemTurretBuild itb) {
                        for (AmmoEntry ae : itb.ammo) {
                            ItemEntry ie = (ItemEntry) ae;
                            fcCall.setTurretAmmo(unit, building, ie.item, 0);
                        }
                    } else {
                        for (Item item : Vars.content.items()) {
                            if (building.items.get(item) > 0) fcCall.setItem(unit, building, item, 0);
                        }
                    }
                }
                if (building.liquids != null) {
                    for (Liquid liquid : Vars.content.liquids()) {
                        if (building.liquids.get(liquid) > 0) fcCall.setLiquid(building, liquid, 0);
                    }
                }
                if (building.power != null && building.block.consPower != null) {
                    fcCall.setPower(building, 0);
                }
            }
        }).width(50f).right().row();
        table.add(bundleNS.get("addToAll")).width(100f).left();
        table.button(bundleNS.get("add"), () -> {
            col.toggle();
        }).width(50f).group(group).right().row();
        table.add(col).center().colspan(2);
    }
}
