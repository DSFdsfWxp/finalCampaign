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
public class setDrillBuildingPreferItemPacket extends fcPacket {
    public mindustry.gen.Building building;
    public mindustry.type.Item v;

    @Override
    public void read(Reads reads) {
        super.read(reads);
        this.building = TypeIO.readBuilding(reads);
        this.v = TypeIO.readItem(reads);
    }

    @Override
    public void write(Writes writes) {
        super.write(writes);
        TypeIO.writeBuilding(writes, this.building);
        TypeIO.writeItem(writes, this.v);
    }

    @Override
    public void handleClient() {
        fcAction.setDrillBuildingPreferItem(this.__caller, this.building, this.v);
    }

    @Override
    public void handleServer(Player player) {
        super.handleServer(player);
        if (fcAction.setDrillBuildingPreferItem(player, this.building, this.v)) fcNet.send(this);
    }

}