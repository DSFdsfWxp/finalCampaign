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
public class setHealthPacket extends fcPacket {
    public mindustry.gen.Building building;
    public float amount;

    @Override
    public void read(Reads reads) {
        super.read(reads);
        this.building = TypeIO.readBuilding(reads);
        this.amount = reads.f();
    }

    @Override
    public void write(Writes writes) {
        super.write(writes);
        TypeIO.writeBuilding(writes, this.building);
        writes.f(this.amount);
    }

    @Override
    public void handleClient() {
        fcAction.setHealth(this.__caller, this.building, this.amount);
    }

    @Override
    public void handleServer(Player player) {
        super.handleServer(player);
        if (fcAction.setHealth(player, this.building, this.amount)) fcNet.send(this);
    }

}