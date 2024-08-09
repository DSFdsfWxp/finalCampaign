package finalCampaign.net;

import mindustry.*;
import finalCampaign.net.packet.*;

// Automatic generated, do not modify.

@SuppressWarnings("all")
public class fcCall {
    public static void setBuildingFilter(mindustry.gen.Building building, byte[] data) {
        setBuildingFilterPacket packet = new setBuildingFilterPacket();
        packet.building = building;
        packet.data = data;

        if (!Vars.net.active() || Vars.net.server())
            packet.handleServer(Vars.player);
        if (Vars.net.client())
            fcNet.send(packet);
    }

    public static void setBuildingForceDisablePredictTarget(mindustry.gen.Building building, boolean v) {
        setBuildingForceDisablePredictTargetPacket packet = new setBuildingForceDisablePredictTargetPacket();
        packet.building = building;
        packet.v = v;

        if (!Vars.net.active() || Vars.net.server())
            packet.handleServer(Vars.player);
        if (Vars.net.client())
            fcNet.send(packet);
    }

    public static void setBuildingSortf(mindustry.gen.Building building, byte[] data) {
        setBuildingSortfPacket packet = new setBuildingSortfPacket();
        packet.building = building;
        packet.data = data;

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

    public static void setForceStatus(mindustry.gen.Building building, boolean forceStatus, boolean forceDisable) {
        setForceStatusPacket packet = new setForceStatusPacket();
        packet.building = building;
        packet.forceStatus = forceStatus;
        packet.forceDisable = forceDisable;

        if (!Vars.net.active() || Vars.net.server())
            packet.handleServer(Vars.player);
        if (Vars.net.client())
            fcNet.send(packet);
    }

    public static void setHealth(mindustry.gen.Building building, float amount) {
        setHealthPacket packet = new setHealthPacket();
        packet.building = building;
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

    public static void setPower(mindustry.gen.Building building, float amount) {
        setPowerPacket packet = new setPowerPacket();
        packet.building = building;
        packet.amount = amount;

        if (!Vars.net.active() || Vars.net.server())
            packet.handleServer(Vars.player);
        if (Vars.net.client())
            fcNet.send(packet);
    }

    public static void setTeam(mindustry.gen.Teamc teamc, mindustry.game.Team team) {
        setTeamPacket packet = new setTeamPacket();
        packet.teamc = teamc;
        packet.team = team;

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

    public static void setTurretAmmoOrder(mindustry.gen.Building building, mindustry.type.Item[] order) {
        setTurretAmmoOrderPacket packet = new setTurretAmmoOrderPacket();
        packet.building = building;
        packet.order = order;

        if (!Vars.net.active() || Vars.net.server())
            packet.handleServer(Vars.player);
        if (Vars.net.client())
            fcNet.send(packet);
    }

    public static void setTurretPreferBuildingTarget(mindustry.gen.Building building, boolean v) {
        setTurretPreferBuildingTargetPacket packet = new setTurretPreferBuildingTargetPacket();
        packet.building = building;
        packet.v = v;

        if (!Vars.net.active() || Vars.net.server())
            packet.handleServer(Vars.player);
        if (Vars.net.client())
            fcNet.send(packet);
    }

    public static void setTurretPreferExtinguish(mindustry.gen.Building building, boolean v) {
        setTurretPreferExtinguishPacket packet = new setTurretPreferExtinguishPacket();
        packet.building = building;
        packet.v = v;

        if (!Vars.net.active() || Vars.net.server())
            packet.handleServer(Vars.player);
        if (Vars.net.client())
            fcNet.send(packet);
    }

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

    public static void register() {
        packets.registerPacket(setBuildingFilterPacket::new);
        packets.registerPacket(setBuildingForceDisablePredictTargetPacket::new);
        packets.registerPacket(setBuildingSortfPacket::new);
        packets.registerPacket(setCurrentLiquidPacket::new);
        packets.registerPacket(setForceStatusPacket::new);
        packets.registerPacket(setHealthPacket::new);
        packets.registerPacket(setItemPacket::new);
        packets.registerPacket(setLiquidPacket::new);
        packets.registerPacket(setPowerPacket::new);
        packets.registerPacket(setTeamPacket::new);
        packets.registerPacket(setTurretAmmoOrderPacket::new);
        packets.registerPacket(setTurretAmmoPacket::new);
        packets.registerPacket(setTurretPreferBuildingTargetPacket::new);
        packets.registerPacket(setTurretPreferExtinguishPacket::new);
        packets.registerPacket(takeLiquidPacket::new);
        packets.registerPacket(takePowerPacket::new);
        packets.registerPacket(takeTurretAmmoPacket::new);
    }
}