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
public class setForceStatusPacket extends fcPacket {
    public mindustry.gen.Building building;
    public boolean forceStatus;
    public boolean forceDisable;

    @Override
    public void read(Reads reads) {
        super.read(reads);
        this.building = TypeIO.readBuilding(reads);
        this.forceStatus = reads.bool();
        this.forceDisable = reads.bool();
    }

    @Override
    public void write(Writes writes) {
        super.write(writes);
        TypeIO.writeBuilding(writes, this.building);
        writes.bool(this.forceStatus);
        writes.bool(this.forceDisable);
    }

    @Override
    public void handleClient() {
        fcAction.setForceStatus(this.__caller, this.building, this.forceStatus, this.forceDisable);
    }

    @Override
    public void handleServer(Player player) {
        super.handleServer(player);
        if (fcAction.setForceStatus(player, this.building, this.forceStatus, this.forceDisable)) fcNet.send(this);
    }

}