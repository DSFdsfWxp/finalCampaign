package finalCampaign.net;

import java.lang.reflect.*;
import arc.util.*;
import finalCampaign.net.fcNet.*;
import finalCampaign.patch.*;
import finalCampaign.util.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.defense.turrets.ItemTurret.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class fcAction {
    private static boolean checkTeam(Teamc ...objs) {
        boolean skipCheck = sandbox() || Vars.state.rules.editor;
        if (objs.length == 0 || skipCheck) return skipCheck;
        Team t = objs[0].team();
        for (Teamc c : objs) if (c.team() != t) return false;
        return true;
    }

    private static boolean sandbox() {
        return Vars.state.rules.mode() == Gamemode.sandbox;
    }

    @CallFrom(PacketSource.both)
    public static boolean takeTurretAmmo(Player player, Unit unit, Building building, Item item, int amount) {
        if (player == null || unit == null || building == null || amount <= 0f) return false;
        if (player.dead() || unit.dead() || building.dead()) return false;
        if (unit.stack.amount > 0 && unit.stack.item != item) return false;
        if (!checkTeam(unit, building) || player.team() != unit.team()) return false;

        if (building instanceof ItemTurretBuild itb) {
            for (AmmoEntry ae : itb.ammo) {
                ItemEntry ie = (ItemEntry) ae;
                if (ie.item != item) continue;

                int capacity = unit.type.itemCapacity - unit.stack.amount;
                amount = Math.min(capacity, amount);
                amount = Math.min(amount, ie.amount);
                ie.amount -= amount;
                unit.addItem(item, amount);
                return true;
            }
        }

        return false;
    }

    @CallFrom(PacketSource.both)
    public static boolean setTurretAmmo(Player player, Unit unit, Building building, Item item, int amount) {
        if (player == null || unit == null || building == null) return false;
        if (player.team() != unit.team()) return false;
        if (building.dead()) return false;
        boolean remove = amount < 0;

        if (remove) {
            amount = -amount;
            if (unit.dead() || player.dead()) return false;
            if (!checkTeam(unit, building)) return false;

            if (building instanceof ItemTurretBuild itb) {
                for (AmmoEntry ae : itb.ammo) {
                    ItemEntry ie = (ItemEntry) ae;
                    if (ie.item != item) continue;
    
                    int capacity = unit.type.itemCapacity - unit.stack.amount;
                    amount = Math.min(capacity, amount);
                    amount = Math.min(amount, ie.amount);
                    ie.amount -= amount;
                    return true;
                }
            }
    
            return false;
        } else {
            if (!sandbox()) return false;

            if (building instanceof ItemTurretBuild itb) {
                for (AmmoEntry ae : itb.ammo) {
                    ItemEntry ie = (ItemEntry) ae;
                    if (ie.item != item) continue;
    
                    ie.amount = amount;
                    return true;
                }
                
                ItemTurret turret = (ItemTurret) itb.block;
                if (turret.ammoTypes.get(item) == null) return false;
                Constructor<ItemEntry> constructor = reflect.getDeclaredConstructor(ItemEntry.class, Item.class, int.class);
                reflect.setAccessible(constructor, true);
                itb.ammo.add(reflect.newInstance(constructor, item, amount));
                return true;
            }
    
            return false;
        }
    }

    @CallFrom(PacketSource.both)
    public static boolean takeLiquid(Player player, Unit unit, Building building, Liquid liquid, float amount) {
        if (player == null || unit == null || building == null || amount <= 0f) return false;
        if (building.liquids == null) return false;
        if (building.liquids.get(liquid) <= 0f) return false;
        if (player.dead() || unit.dead() || building.dead()) return false;
        if (!checkTeam(unit, building) || unit.team() != player.team()) return false;

        float capacity = unit.type.itemCapacity;
        amount = Math.min(amount, building.liquids.get(liquid));
        amount = Math.min(capacity, amount);
        building.liquids.remove(liquid, amount);
        unit.apply(liquid.effect, amount / 60f);
        return true;
    }

    @CallFrom(PacketSource.both)
    public static boolean setLiquid(Player player, Building building, Liquid liquid, float amount) {
        if (player == null || building == null || amount <= 0f) return false;
        if (building.liquids == null) return false;
        if (player.dead() || building.dead()) return false;
        if (!sandbox()) return false;
        if (!building.block.consumesLiquid(liquid)) return false;

        building.liquids.set(liquid, amount);
        return true;
    }

    @CallFrom(PacketSource.both)
    public static boolean takePower(Player player, Unit unit, Building building, float amount) {
        if (player == null || unit == null || building == null || amount <= 0f) return false;
        if (unit.dead() || player.dead() || building.dead()) return false;
        if (building.power == null || building.block.consPower == null) return false;
        if (!checkTeam(unit, building) || unit.team() != player.team()) return false;

        float capacity = unit.type.itemCapacity;
        float current = building.power.status * building.block.consPower.capacity;
        amount = Math.min(current, amount);
        amount = Math.min(amount, capacity);
        
        building.power.status = (current - amount) / building.block.consPower.capacity;
        unit.apply(Vars.content.statusEffect("electrified"), amount / 60f);
        return true;
    }

    @CallFrom(PacketSource.both)
    public static boolean setPower(Player player, Building building, float amount) {
        if (player == null || building == null || amount <= 0f) return false;
        if (player.dead() || building.dead()) return false;
        if (building.power == null || building.block.consPower == null) return false;
        if (!sandbox()) return false;
        
        IFcBuilding f = (IFcBuilding) building;
        building.power.status = amount / building.block.consPower.capacity;
        f.fcInfinityPower(amount == Float.POSITIVE_INFINITY);
        return true;
    }

    @CallFrom(PacketSource.both)
    public static boolean setItem(Player player, Unit unit, Building building, Item item, int amount) {
        if (player == null || unit == null || building == null || amount <= 0) return false;
        if (building.items == null) return false;
        if (building.dead()) return false;
        if (player.team() != unit.team()) return false;
        boolean remove = amount < 0;

        if (remove) {
            amount = -amount;
            if (unit.dead() || player.dead()) return false;
            if (!checkTeam(unit, building)) return false;

            int capacity = unit.type.itemCapacity - unit.stack.amount;
            amount = Math.min(amount, capacity);
            amount = Math.min(amount, building.items.get(item));

            building.items.remove(item, amount);
            return true;
        } else {
            if (!sandbox()) return false;
            if (!building.block.consumesItem(item)) return false;

            building.items.set(item, amount);
            return true;
        }
    }

    @CallFrom(PacketSource.both)
    public static boolean setTurretAmmoOrder(Player player, Building building, Item[] order) {
        if (player == null || building == null || order.length == 0) return false;
        if (player.dead() || building.dead()) return false;
        if (!checkTeam(player.unit(), building)) return false;

        if (building instanceof ItemTurretBuild itb) {
            ItemEntry[] entries = new ItemEntry[order.length];
            for (int i=order.length - 1; i>=0; i--) {
                for (AmmoEntry ae : itb.ammo) {
                    ItemEntry ie = (ItemEntry) ae;
                    if (ie.item != order[i]) continue;
                    entries[i] = ie;
                    break;
                }
                if (entries[i] != null) itb.ammo.remove(entries[i]);
            }
            for (int i=order.length - 1; i>=0; i--) if (entries[i] != null) itb.ammo.add(entries[i]);

            return true;
        }

        return false;
    }

    @CallFrom(PacketSource.both)
    public static boolean setCurrentLiquid(Player player, Building building, Liquid liquid) {
        if (player == null || building == null) return false;
        if (player.dead()) return false;
        if (!checkTeam(player.unit(), building)) return false;
        if (building.liquids == null) return false;
        
        if (building.liquids.current() == liquid) return false;
        Reflect.set(building.liquids, "current", liquid);

        return true;
    }

    @CallFrom(PacketSource.both)
    public static boolean setForceStatus(Player player, Building building, boolean forceStatus, boolean forceDisable) {
        if (player == null || building == null) return false;
        if (player.dead()) return false;
        if (!checkTeam(player.unit(), building)) return false;

        IFcBuilding f = (IFcBuilding) building;
        if (forceStatus) {
            f.fcForceDisable(false);
            f.fcForceEnable(false);
        } else {
            f.fcForceDisable(forceDisable);
            f.fcForceEnable(!forceDisable);
        }

        return true;
    }

    @CallFrom(PacketSource.both)
    public static boolean setHealth(Player player, Building building, float amount) {
        if (player == null || building == null) return false;
        if (player.dead()) return false;
        if (!sandbox()) return false;

        if (amount < 0) amount = 0f;
        building.health = amount;

        return true;
    }
}
