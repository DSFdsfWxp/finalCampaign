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
public class setTeamPacket extends fcPacket {
    public mindustry.gen.Teamc teamc;
    public mindustry.game.Team team;

    @Override
    public void read(Reads reads) {
        super.read(reads);
        this.teamc = typeIO.readTeamc(reads);
        this.team = TypeIO.readTeam(reads);
    }

    @Override
    public void write(Writes writes) {
        super.write(writes);
        typeIO.writeTeamc(writes, this.teamc);
        TypeIO.writeTeam(writes, this.team);
    }

    @Override
    public void handleClient() {
        fcAction.setTeam(this.__caller, this.teamc, this.team);
    }

    @Override
    public void handleServer(Player player) {
        super.handleServer(player);
        if (fcAction.setTeam(player, this.teamc, this.team)) fcNet.send(this);
    }

}