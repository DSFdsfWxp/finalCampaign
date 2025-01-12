package finalCampaign.net;

import java.lang.reflect.*;
import arc.util.*;
import finalCampaign.annotation.net.*;
import finalCampaign.feature.buildTargeting.*;
import finalCampaign.map.*;
import finalCampaign.patch.*;
import finalCampaign.util.*;
import mindustry.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.defense.turrets.ItemTurret.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class fcAction {

    @netCall(src = packetSource.both)
    @buildingTarget(IFcTurretBuild.class)
    public static boolean takeTurretAmmo(Player player, Unit unit, Building building, Item item, int amount) {
        if (amount <= 0f) return false;
        if (building.block == null) return false;
        if (player.dead()) return false;
        if (unit.stack.amount > 0 && unit.stack.item != item) return false;

        if (building instanceof ItemTurretBuild itb) {
            ItemTurret it = (ItemTurret) itb.block;
            BulletType bt = it.ammoTypes.get(item);
            if (bt == null) return false;

            for (AmmoEntry ae : itb.ammo) {
                ItemEntry ie = (ItemEntry) ae;
                if (ie.item != item) continue;

                amount = Math.min(amount, ie.amount);
                int capacity = unit.type.itemCapacity - unit.stack.amount;
                amount = Math.min((int)(capacity * bt.ammoMultiplier), amount);
                amount = Math.min(amount, (int)(unit.maxAccepted(item) * bt.ammoMultiplier));
                if (ie.amount < Short.MAX_VALUE) {
                    ie.amount -= amount;
                    itb.totalAmmo -= amount;
                }
                unit.addItem(item, (int)(amount / bt.ammoMultiplier));
                if (ie.amount <= 0f) itb.ammo.remove(ie);
                return true;
            }
        }

        return false;
    }

    @netCall(src = packetSource.both)
    @buildingTarget(IFcTurretBuild.class)
    public static boolean setTurretAmmo(Player player, Unit unit, Building building, Item item, int amount) {
        boolean remove = amount < 0;

        if (remove) {
            amount = -amount;

            if (building instanceof ItemTurretBuild itb) {
                ItemTurret it = (ItemTurret) itb.block;
                itb.noSleep();
                for (AmmoEntry ae : itb.ammo) {
                    ItemEntry ie = (ItemEntry) ae;
                    if (ie.item != item) continue;
    
                    int capacity = unit.type.itemCapacity - unit.stack.amount;
                    amount = Math.min(capacity, amount);
                    amount = Math.min(amount, ie.amount);
                    ie.amount -= amount;
                    itb.totalAmmo -= amount;
                    itb.totalAmmo = Math.min(itb.totalAmmo, it.maxAmmo);
                    if (ie.amount <= 0f) itb.ammo.remove(ie);
                    return true;
                }
            }
    
            return false;
        } else {
            if (!fcMap.sandbox()) return false;

            if (building instanceof ItemTurretBuild itb) {
                ItemTurret it = (ItemTurret) itb.block;
                itb.noSleep();

                int capacity = it.maxAmmo;
                int currentAmount = 0;

                for (AmmoEntry ae : itb.ammo) {
                    ItemEntry ie = (ItemEntry) ae;
                    currentAmount += ie.amount > 0 ? ie.amount : 0;
                }
                currentAmount = Math.min(capacity, currentAmount);

                if (amount == Short.MAX_VALUE) itb.ammo.clear();

                for (AmmoEntry ae : itb.ammo) {
                    ItemEntry ie = (ItemEntry) ae;
                    if (ie.item != item) continue;
    
                    amount = Math.min(currentAmount - ie.amount + amount, capacity) - (currentAmount - ie.amount);
                    if (amount < 0) amount = 0;
                    int d = amount - ie.amount;
                    ie.amount = amount;
                    itb.totalAmmo += d;
                    itb.totalAmmo = Math.min(itb.totalAmmo, it.maxAmmo);
                    if (itb.totalAmmo < 0) itb.totalAmmo = 0;
                    if (ie.amount <= 0f) itb.ammo.remove(ie);
                    return true;
                }
                
                ItemTurret turret = (ItemTurret) itb.block;
                if (turret.ammoTypes.get(item) == null) return false;
                Constructor<?> constructor = reflect.getDeclaredConstructors(ItemEntry.class)[0];
                reflect.setAccessible(constructor, true);
                if (amount != Short.MAX_VALUE) amount = Math.min(amount, capacity - currentAmount);
                if (amount <= 0) return false;
                itb.ammo.add((ItemEntry) reflect.newInstance(constructor, building.block, item, amount));
                itb.totalAmmo = amount == Short.MAX_VALUE ? it.maxAmmo : itb.totalAmmo + amount;
                itb.totalAmmo = Math.min(itb.totalAmmo, it.maxAmmo);
                return true;
            }
    
            return false;
        }
    }

    @netCall(src = packetSource.both)
    public static boolean takeLiquid(Player player, Unit unit, Building building, Liquid liquid, float amount) {
        if (amount <= 0f) return false;
        if (building.liquids.get(liquid) <= 0f) return false;
        if (player.dead()) return false;

        float capacity = unit.type.itemCapacity;
        amount = Math.min(amount, building.liquids.get(liquid));
        amount = Math.min(capacity, amount);
        building.liquids.remove(liquid, amount);
        unit.apply(liquid.effect, amount);
        return true;
    }

    @netCall(src = packetSource.both)
    @sandboxOnly
    public static boolean setLiquid(Player player, Building building, Liquid liquid, float amount) {
        if (amount < 0f) return false;
        if (!building.block.consumesLiquid(liquid) && !building.acceptLiquid(building, liquid) && building.liquids.get(liquid) < amount) return false;
        
        if (amount != Float.POSITIVE_INFINITY) amount = Math.min(amount, building.block.liquidCapacity);
        if (amount < 0) amount = 0;
        building.liquids.set(liquid, amount);
        return true;
    }

    @netCall(src = packetSource.both)
    public static boolean takePower(Player player, Unit unit, Building building, float amount) {
        if (amount <= 0f) return false;
        if (player.dead()) return false;
        if (building.power == null || building.block.consPower == null) return false;

        float current = building.power.status * Math.max(building.block.consPower.capacity, building.block.consPower.usage);
        amount = Math.min(current, amount);
        
        building.power.status = (current - amount) / Math.max(building.block.consPower.capacity, building.block.consPower.usage);
        unit.damage(amount);
        return true;
    }

    @netCall(src = packetSource.both)
    @sandboxOnly
    public static boolean setPower(Player player, Building building, float amount) {
        if (amount < 0f) return false;
        if (building.power == null || building.block.consPower == null) return false;
        
        IFcBuilding f = (IFcBuilding) building;
        float capacity = Math.max(building.block.consPower.capacity, building.block.consPower.usage);
        if (amount != Float.POSITIVE_INFINITY) amount = Math.min(capacity, amount);
        if (amount < 0) amount = 0;
        building.power.status = amount / capacity;
        f.fcInfinityPower(amount == Float.POSITIVE_INFINITY);
        return true;
    }

    @netCall(src = packetSource.both)
    public static boolean setItem(Player player, Unit unit, Building building, Item item, int amount) {
        boolean remove = amount < 0;

        if (remove) {
            amount = -amount;

            int capacity = unit.type.itemCapacity - unit.stack.amount;
            amount = Math.min(amount, capacity);
            amount = Math.min(amount, building.items.get(item));

            building.items.remove(item, amount);
            return true;
        } else {
            if (!fcMap.sandbox()) return false;
            if (!building.block.consumesItem(item) && !building.acceptItem(building, item) && building.items.get(item) < amount) return false;

            int capacity = building.block.itemCapacity;
            if (amount != Integer.MAX_VALUE) amount = Math.min(amount, capacity);
            if (amount < 0) amount = 0;
            building.items.set(item, amount);
            return true;
        }
    }

    @netCall(src = packetSource.both)
    @buildingTarget(IFcTurretBuild.class)
    public static boolean setTurretAmmoOrder(Player player, Building building, Item[] order) {
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

    @netCall(src = packetSource.both)
    public static boolean setCurrentLiquid(Player player, Building building, Liquid liquid) {
        if (building.liquids.current() == liquid) return false;
        Reflect.set(building.liquids, "current", liquid);

        return true;
    }

    @netCall(src = packetSource.both)
    public static boolean setForceStatus(Player player, Building building, boolean forceStatus, boolean forceDisable) {
        IFcBuilding f = (IFcBuilding) building;
        if (!forceStatus) {
            f.fcForceDisable(false);
            f.fcForceEnable(false);
            building.enabled = true;
        } else {
            f.fcForceDisable(forceDisable);
            f.fcForceEnable(!forceDisable);
        }
        building.noSleep();

        return true;
    }

    @netCall(src = packetSource.both)
    @sandboxOnly
    public static boolean setHealth(Player player, Building building, float amount) {
        if (amount < 0) amount = 0f;
        building.health = amount;

        return true;
    }

    @netCall(src = packetSource.both)
    @sandboxOnly
    public static boolean setTeam(Player player, Teamc teamc, Team team) {
        if (teamc instanceof Building building) {
            building.changeTeam(team);
        }

        return true;
    }

    @netCall(src = packetSource.both)
    @buildingTarget(IFcTurretBuild.class)
    public static boolean setBuildingForceDisablePredictTarget(Player player, Building building, boolean v) {
        IFcTurretBuild f = (IFcTurretBuild) building;
        f.fcForceDisablePredictTarget(v);

        return true;
    }

    @netCall(src = packetSource.both)
    @buildingTarget(IFcTurretBuild.class)
    public static boolean setBuildingSortf(Player player, Building building, byte[] data) {
        IFcTurretBuild f = (IFcTurretBuild) building;
        fcSortf sortf = f.fcSortf();

        byte[] bak = sortf.write();
        sortf.read(data);

        if (!sortf.isValid()) {
            sortf.read(bak);
            return false;
        }

        if (Vars.net.client() || !Vars.net.active()) {
            if (player.id == Vars.player.id) {
                IFcTurret fBlock = (IFcTurret) building.block;
                fBlock.fcSortf(data);
            }
        }
        return true;
    }

    @netCall(src = packetSource.both)
    @buildingTarget(IFcTurretBuild.class)
    public static boolean setTurretPreferBuildingTarget(Player player, Building building, boolean v) {
        IFcTurretBuild f = (IFcTurretBuild) building;
        f.fcPreferBuildingTarget(v);
        
        if (Vars.net.client() || !Vars.net.active()) {
            if (player.id == Vars.player.id) {
                IFcTurret fBlock = (IFcTurret) building.block;
                fBlock.fcPreferBuildingTarget(v);
            }
        }
        
        return true;
    }

    @netCall(src = packetSource.both)
    @buildingTarget(IFcLiquidTurretBuild.class)
    public static boolean setTurretPreferExtinguish(Player player, Building building, boolean v) {
        IFcLiquidTurretBuild f = (IFcLiquidTurretBuild) building;
        f.fcPreferExtinguish(v);

        if (Vars.net.client() || !Vars.net.active()) {
            if (player.id == Vars.player.id) {
                IFcLiquidTurret fBlock = (IFcLiquidTurret) building.block;
                fBlock.fcPreferExtinguish(v);
            }
        }

        return true;
    }

    @netCall(src = packetSource.both)
    @buildingTarget(IFcTurretBuild.class)
    public static boolean setBuildingFilter(Player player, Building building, byte[] data) {
        IFcTurretBuild f = (IFcTurretBuild) building;
        f.fcFilter().read(data);

        return true;
    }

    @netCall(src = packetSource.both)
    @buildingTarget(IFcDrillBuild.class)
    public static boolean setDrillBuildingPreferItem(Player player, Building building, Item v) {
        IFcDrillBuild b = (IFcDrillBuild) building;
        b.fcPreferItem(v);

        return true;
    }
}
