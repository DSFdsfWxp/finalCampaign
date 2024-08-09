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
public class setTurretPreferExtinguishPacket extends fcPacket {
    public mindustry.gen.Building building;
    public boolean v;

    @Override
    public void read(Reads reads) {
        super.read(reads);
        this.building = TypeIO.readBuilding(reads);
        this.v = reads.bool();
    }

    @Override
    public void write(Writes writes) {
        super.write(writes);
        TypeIO.writeBuilding(writes, this.building);
        writes.bool(this.v);
    }

    @Override
    public void handleClient() {
        fcAction.setTurretPreferExtinguish(this.__caller, this.building, this.v);
    }

    @Override
    public void handleServer(Player player) {
        super.handleServer(player);
        if (fcAction.setTurretPreferExtinguish(player, this.building, this.v)) fcNet.send(this);
    }

}