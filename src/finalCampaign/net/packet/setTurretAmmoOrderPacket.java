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
public class setTurretAmmoOrderPacket extends fcPacket {
    public mindustry.gen.Building building;
    public mindustry.type.Item[] order;

    @Override
    public void read(Reads reads) {
        super.read(reads);
        this.building = TypeIO.readBuilding(reads);
        this.order = typeIO.readItems(reads);
    }

    @Override
    public void write(Writes writes) {
        super.write(writes);
        TypeIO.writeBuilding(writes, this.building);
        typeIO.writeItems(writes, this.order);
    }

    @Override
    public void handleClient() {
        fcAction.setTurretAmmoOrder(this.__caller, this.building, this.order);
    }

    @Override
    public void handleServer(Player player) {
        super.handleServer(player);
        if (fcAction.setTurretAmmoOrder(player, this.building, this.order)) fcNet.send(this);
    }

}