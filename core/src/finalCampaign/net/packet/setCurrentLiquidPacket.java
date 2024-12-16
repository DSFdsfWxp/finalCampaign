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
public class setCurrentLiquidPacket extends fcPacket {
    public mindustry.gen.Building building;
    public mindustry.type.Liquid liquid;

    @Override
    public void read(Reads reads) {
        super.read(reads);
        this.building = TypeIO.readBuilding(reads);
        this.liquid = TypeIO.readLiquid(reads);
    }

    @Override
    public void write(Writes writes) {
        super.write(writes);
        TypeIO.writeBuilding(writes, this.building);
        TypeIO.writeLiquid(writes, this.liquid);
    }

    @Override
    public void handleClient() {
        fcAction.setCurrentLiquid(this.__caller, this.building, this.liquid);
    }

    @Override
    public void handleServer(Player player) {
        super.handleServer(player);
        if (fcAction.setCurrentLiquid(player, this.building, this.liquid)) fcNet.send(this);
    }

}