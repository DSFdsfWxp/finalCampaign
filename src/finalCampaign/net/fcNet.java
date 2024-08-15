package finalCampaign.net;

import java.io.*;
import java.lang.annotation.*;
import java.nio.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.gen.*;

// Not registering packets in Mindustry.net.Net because the max packet id is 127, the game has used 114
// ids and the additional ids registered by mods may be different between different clients.

public class fcNet {
    private static final String type = "finalCampaign.Net";

    public static void send(fcPacket packet) {
        if (!Vars.net.active()) return;

        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        Writes writes = new Writes(new DataOutputStream(bs));
        packet.write(writes);
        writes.close();

        byte[] data = bs.toByteArray();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Writes w = new Writes(new DataOutputStream(os));
        w.b(packets.getPacketId(packet));
        w.i(data.length);
        w.b(data);
        if (os.size() % 2 != 0) w.b(0);
        w.close();

        CharBuffer cb = ByteBuffer.wrap(os.toByteArray()).asCharBuffer();
        int clen = cb.length();
        char[] carr = new char[clen];
        for (int i=0; i<clen; i++) carr[i] = cb.get(i);
        String txt = String.valueOf(carr);

        boolean reliable = true;

        for (Annotation annotation : packet.getClass().getAnnotations())
            if (annotation instanceof CallFrom cf) reliable = cf.reliable();

        if (Vars.net.client()) {
            if (reliable) {
                Call.serverPacketReliable(type, txt);
            } else {
                Call.serverPacketUnreliable(type, txt);
            }
        } else {
            if (reliable) {
                Call.clientPacketReliable(type, txt);
            } else {
                Call.clientPacketUnreliable(type, txt);
            }
        }
    }

    private static fcPacket read(String txt) {
        char[] src = new char[txt.length()];
        txt.getChars(0, src.length, src, 0);

        byte[] data = new byte[src.length * 2];
        for (int i=0; i<src.length; i++) {
            char c = src[i];
            data[i * 2] = (byte) (c >>> 8);
            data[i * 2 + 1] = (byte) (c & 0xff);
        }

        Reads r = new Reads(new DataInputStream(new ByteArrayInputStream(data)));
        byte id = r.b();
        int len = r.i();
        byte[] packetData = r.b(len);
        r.close();

        Reads reads = new Reads(new DataInputStream(new ByteArrayInputStream(packetData)));
        fcPacket packet = packets.newPacket(id);
        packet.read(reads, len);
        try {
            reads.close();
        } catch(Throwable ignore) {}

        return packet;
    }

    private static void clientReceive(String txt) {
        fcPacket packet = read(txt);
        Log.debug("Client recieve: " + packet.getClass().getName() + ", " + txt.length());
        for (Annotation annotation : packet.getClass().getAnnotations())
            if (annotation instanceof CallFrom cf) if (cf.value() == PacketSource.client) return;
        
        try {
            packet.handleClient();
        } catch(Exception e) {
            Log.err(e);
        }
    }

    private static void serverReceive(Player player, String txt) {
        fcPacket packet = read(txt);
        Log.debug("Server recieve: " + packet.getClass().getName() + ", " + txt.length());
        for (Annotation annotation : packet.getClass().getAnnotations())
            if (annotation instanceof CallFrom cf) if (cf.value() == PacketSource.server) return;
        
        try {
            packet.handleServer(player);
        } catch(Exception e) {
            Log.err(e);
        }
    }

    public static void register() {
        if (Vars.netClient != null) Vars.netClient.addPacketHandler(type, fcNet::clientReceive);
        if (Vars.netServer != null) Vars.netServer.addPacketHandler(type, fcNet::serverReceive);
    }

    public static enum PacketSource {
        client,
        server,
        both
    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface CallFrom {
        public PacketSource value();
        public boolean reliable() default true;
    }
}
