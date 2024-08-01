package finalCampaign.net;

import mindustry.*;
import finalCampaign.net.packet.*;

// Automatic generated, do not modify.

@SuppressWarnings("all")
public class fcCall {
    public static void takeLiquid(mindustry.gen.Unit unit, mindustry.gen.Building building, mindustry.type.Liquid liquid, float amount) {
        takeLiquidPacket packet = new takeLiquidPacket();
        packet.unit = unit;
        packet.building = building;
        packet.liquid = liquid;
        packet.amount = amount;

        if (!Vars.net.active() || Vars.net.server())
            packet.handleServer(Vars.player);
        if (Vars.net.client())
            fcNet.send(packet);
    }

    public static void takePower(mindustry.gen.Unit unit, mindustry.gen.Building building, float amount) {
        takePowerPacket packet = new takePowerPacket();
        packet.unit = unit;
        packet.building = building;
        packet.amount = amount;

        if (!Vars.net.active() || Vars.net.server())
            packet.handleServer(Vars.player);
        if (Vars.net.client())
            fcNet.send(packet);
    }

    public static void setLiquid(mindustry.gen.Building building, mindustry.type.Liquid liquid, float amount) {
        setLiquidPacket packet = new setLiquidPacket();
        packet.building = building;
        packet.liquid = liquid;
        packet.amount = amount;

        if (!Vars.net.active() || Vars.net.server())
            packet.handleServer(Vars.player);
        if (Vars.net.client())
            fcNet.send(packet);
    }

    public static void setItem(mindustry.gen.Unit unit, mindustry.gen.Building building, mindustry.type.Item item, int amount) {
        setItemPacket packet = new setItemPacket();
        packet.unit = unit;
        packet.building = building;
        packet.item = item;
        packet.amount = amount;

        if (!Vars.net.active() || Vars.net.server())
            packet.handleServer(Vars.player);
        if (Vars.net.client())
            fcNet.send(packet);
    }

    public static void setTurretAmmoOrder(mindustry.gen.Building building, mindustry.type.Item[] order) {
        setTurretAmmoOrderPacket packet = new setTurretAmmoOrderPacket();
        packet.building = building;
        packet.order = order;

        if (!Vars.net.active() || Vars.net.server())
            packet.handleServer(Vars.player);
        if (Vars.net.client())
            fcNet.send(packet);
    }

    public static void takeTurretAmmo(mindustry.gen.Unit unit, mindustry.gen.Building building, mindustry.type.Item item, int amount) {
        takeTurretAmmoPacket packet = new takeTurretAmmoPacket();
        packet.unit = unit;
        packet.building = building;
        packet.item = item;
        packet.amount = amount;

        if (!Vars.net.active() || Vars.net.server())
            packet.handleServer(Vars.player);
        if (Vars.net.client())
            fcNet.send(packet);
    }

    public static void setCurrentLiquid(mindustry.gen.Building building, mindustry.type.Liquid liquid) {
        setCurrentLiquidPacket packet = new setCurrentLiquidPacket();
        packet.building = building;
        packet.liquid = liquid;

        if (!Vars.net.active() || Vars.net.server())
            packet.handleServer(Vars.player);
        if (Vars.net.client())
            fcNet.send(packet);
    }

    public static void setTurretAmmo(mindustry.gen.Unit unit, mindustry.gen.Building building, mindustry.type.Item item, int amount) {
        setTurretAmmoPacket packet = new setTurretAmmoPacket();
        packet.unit = unit;
        packet.building = building;
        packet.item = item;
        packet.amount = amount;

        if (!Vars.net.active() || Vars.net.server())
            packet.handleServer(Vars.player);
        if (Vars.net.client())
            fcNet.send(packet);
    }

    public static void register() {
        packets.registerPacket(setLiquidPacket::new);
        packets.registerPacket(setTurretAmmoPacket::new);
        packets.registerPacket(takeLiquidPacket::new);
        packets.registerPacket(takePowerPacket::new);
        packets.registerPacket(takeTurretAmmoPacket::new);
        packets.registerPacket(setTurretAmmoOrderPacket::new);
        packets.registerPacket(setCurrentLiquidPacket::new);
        packets.registerPacket(setItemPacket::new);
    }
}