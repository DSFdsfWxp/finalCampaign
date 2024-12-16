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
public class setBuildingSortfPacket extends fcPacket {
    public mindustry.gen.Building building;
    public byte[] data;

    @Override
    public void read(Reads reads) {
        super.read(reads);
        this.building = TypeIO.readBuilding(reads);
        this.data = TypeIO.readBytes(reads);
    }

    @Override
    public void write(Writes writes) {
        super.write(writes);
        TypeIO.writeBuilding(writes, this.building);
        TypeIO.writeBytes(writes, this.data);
    }

    @Override
    public void handleClient() {
        fcAction.setBuildingSortf(this.__caller, this.building, this.data);
    }

    @Override
    public void handleServer(Player player) {
        super.handleServer(player);
        if (fcAction.setBuildingSortf(player, this.building, this.data)) fcNet.send(this);
    }

}