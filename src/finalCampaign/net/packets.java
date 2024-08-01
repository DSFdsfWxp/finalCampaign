package finalCampaign.net;

import arc.func.*;
import arc.struct.*;

public class packets {
    private static Seq<Prov<? extends fcPacket>> packetProvs = new Seq<>();
    private static ObjectIntMap<Class<?>> packetToId = new ObjectIntMap<>();

    public static <T extends fcPacket> void registerPacket(Prov<T> cons) {
        packetProvs.add(cons);
        fcPacket packet = (fcPacket)cons.get();
        packetToId.put(packet.getClass(), packetProvs.size - 1);
    }

    public static byte getPacketId(fcPacket packet) {
        int id = packetToId.get(packet.getClass(), -1);
        if (id == -1)
            throw new RuntimeException("Unknown packet type: " + packet.getClass()); 
        return (byte)id;
    }

    @SuppressWarnings("unchecked")
    public static <T extends fcPacket> T newPacket(byte id) {
        return (T)((Prov<?>)packetProvs.get(id & 0xFF)).get();
    }
}
