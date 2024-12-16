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
public class takePowerPacket extends fcPacket {
    public mindustry.gen.Unit unit;
    public mindustry.gen.Building building;
    public float amount;

    @Override
    public void read(Reads reads) {
        super.read(reads);
        this.unit = TypeIO.readUnit(reads);
        this.building = TypeIO.readBuilding(reads);
        this.amount = reads.f();
    }

    @Override
    public void write(Writes writes) {
        super.write(writes);
        TypeIO.writeUnit(writes, this.unit);
        TypeIO.writeBuilding(writes, this.building);
        writes.f(this.amount);
    }

    @Override
    public void handleClient() {
        fcAction.takePower(this.__caller, this.unit, this.building, this.amount);
    }

    @Override
    public void handleServer(Player player) {
        super.handleServer(player);
        if (fcAction.takePower(player, this.unit, this.building, this.amount)) fcNet.send(this);
    }

}