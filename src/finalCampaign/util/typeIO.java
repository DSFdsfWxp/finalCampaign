package finalCampaign.util;

import java.lang.reflect.Constructor;

import arc.util.io.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.type.*;

public class typeIO {
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
        Constructor<Player> con = reflect.getDeclaredConstructor(Player.class);
        reflect.setAccessible(con, true);
        Player player = reflect.newInstance(con);
        player.read(reads);
        return player;
    }

    public static void writePlayer(Writes writes, Player player) {
        player.write(writes);
    }
}
