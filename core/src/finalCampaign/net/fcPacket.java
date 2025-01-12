package finalCampaign.net;

import arc.util.io.*;
import finalCampaign.util.io.*;
import mindustry.gen.*;
import mindustry.net.*;

public abstract class fcPacket extends Packet {
    protected Player __caller;

    @Override
    public void read(Reads reads) {
        if (reads.bool()) __caller = typeIO.readPlayer(reads);
    }

    @Override
    public void write(Writes writes) {
        boolean vaild = __caller != null;
        if (vaild) vaild = !__caller.dead();
        writes.bool(vaild);
        if (vaild) typeIO.writePlayer(writes, __caller);
    }

    public void handleServer(Player player) {
        __caller = player;
    }
}
