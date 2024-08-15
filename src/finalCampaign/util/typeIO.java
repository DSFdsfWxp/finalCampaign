package finalCampaign.util;

import java.lang.reflect.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.type.*;

public class typeIO {
    private static Constructor<Player> playerCon;

    public static Item[] readItems(Reads reads) {
        int len = reads.i();
        Item[] out = new Item[len];
        for (int i=0; i<len; i++) out[i] = TypeIO.readItem(reads);
        return out;
    }

    public static void writeItems(Writes writes, Item[] items) {
        writes.i(items.length);
        for (Item item : items) TypeIO.writeItem(writes, item);
    }

    public static Player readPlayer(Reads reads) {
        if (playerCon == null) {
            playerCon = reflect.getDeclaredConstructor(Player.class);
            reflect.setAccessible(playerCon, true);
        }
        Player player = reflect.newInstance(playerCon);
        player.read(reads);
        return player;
    }

    public static void writePlayer(Writes writes, Player player) {
        player.write(writes);
    }

    public static Teamc readTeamc(Reads reads) {
        boolean isUnit = reads.bool();
        if (isUnit) return TypeIO.readUnit(reads);
        return TypeIO.readBuilding(reads);
    }

    public static void writeTeamc(Writes writes, Teamc teamc) {
        if (teamc instanceof Building building) {
            writes.bool(false);
            TypeIO.writeBuilding(writes, building);
        } else if (teamc instanceof Unit unit) {
            writes.bool(true);
            TypeIO.writeUnit(writes, unit);
        } else {
            throw new RuntimeException("Not supported Teamc: " + teamc.toString());
        }
    }
}
