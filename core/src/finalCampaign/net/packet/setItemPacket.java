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
public class setItemPacket extends fcPacket {
    public mindustry.gen.Unit unit;
    public mindustry.gen.Building building;
    public mindustry.type.Item item;
    public int amount;

    @Override
    public void read(Reads reads) {
        super.read(reads);
        this.unit = TypeIO.readUnit(reads);
        this.building = TypeIO.readBuilding(reads);
        this.item = TypeIO.readItem(reads);
        this.amount = reads.i();
    }

    @Override
    public void write(Writes writes) {
        super.write(writes);
        TypeIO.writeUnit(writes, this.unit);
        TypeIO.writeBuilding(writes, this.building);
        TypeIO.writeItem(writes, this.item);
        writes.i(this.amount);
    }

    @Override
    public void handleClient() {
        fcAction.setItem(this.__caller, this.unit, this.building, this.item, this.amount);
    }

    @Override
    public void handleServer(Player player) {
        super.handleServer(player);
        if (fcAction.setItem(player, this.unit, this.building, this.item, this.amount)) fcNet.send(this);
    }

}