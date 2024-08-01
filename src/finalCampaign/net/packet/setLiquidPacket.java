package finalCampaign.net.packet;

import mindustry.*;
import finalCampaign.net.*;
import finalCampaign.util.*;
import finalCampaign.net.fcNet.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.type.*;

// Automatic generated, do not modify.

@SuppressWarnings("all")
@CallFrom(PacketSource.both)
public class setLiquidPacket extends fcPacket {
    public mindustry.gen.Building building;
    public mindustry.type.Liquid liquid;
    public float amount;

    @Override
    public void read(Reads reads) {
        super.read(reads);
        this.building = TypeIO.readBuilding(reads);
        this.liquid = TypeIO.readLiquid(reads);
        this.amount = reads.f();
    }

    @Override
    public void write(Writes writes) {
        super.write(writes);
        TypeIO.writeBuilding(writes, this.building);
        TypeIO.writeLiquid(writes, this.liquid);
        writes.f(this.amount);
    }

    @Override
    public void handleClient() {
        fcAction.setLiquid(this.__caller, this.building, this.liquid, this.amount);
    }

    @Override
    public void handleServer(Player player) {
        super.handleServer(player);
        if (fcAction.setLiquid(player, this.building, this.liquid, this.amount)) fcNet.send(this);
    }

}