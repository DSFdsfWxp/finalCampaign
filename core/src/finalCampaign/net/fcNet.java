package finalCampaign.net;

import java.io.*;
import java.lang.annotation.*;
import java.nio.charset.*;
import arc.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.gen.*;

// Notice:
// Not registering packets in Mindustry.net.Net because the max packet id is 127, the game has used 114
// ids and the additional ids registered by mods may be different between different clients.

public class fcNet implements ApplicationListener {
    private static final String type = "finalCampaign.Net";
    private static Seq<byte[]> reliableQueue = new Seq<>();
    private static Seq<byte[]> unreliableQueue = new Seq<>();

    public static void send(fcPacket packet) {
        if (!Vars.net.active()) return;

        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        Writes w = new Writes(new DataOutputStream(bs));
        w.b(packets.getPacketId(packet));
        packet.write(w);
        w.close();

        boolean reliable = true;

        for (Annotation annotation : packet.getClass().getAnnotations())
            if (annotation instanceof CallFrom cf) reliable = cf.reliable();

        if (reliable) {
            reliableQueue.add(bs.toByteArray());
        } else {
            unreliableQueue.add(bs.toByteArray());
        }

        /*
        if (Vars.net.client()) {
            finalCampaign.util.debug.printStackTrace();
        }
        */
    }

    private static fcPacket[] read(String txt) {
        Reads r = new Reads(new DataInputStream(new ByteArrayInputStream(str2Byte(txt))));
        int c = r.i();
        fcPacket[] res = new fcPacket[c];

        for (int i=0; i<c; i++) {
            byte id = r.b();
            fcPacket packet = packets.newPacket(id);
            packet.read(r);
            res[i] = packet;
        }

        r.close();
        return res;
    }

    private static void clientReceive(String txt) {
        fcPacket[] packets = read(txt);
        //Log.debug("Client recieved, len: " + txt.length());
        for (fcPacket packet : packets) {
            //Log.debug(packet.getClass().getName());
            for (Annotation annotation : packet.getClass().getAnnotations())
                if (annotation instanceof CallFrom cf) if (cf.value() == PacketSource.client) return;
            
            try {
                packet.handleClient();
            } catch(Exception e) {
                Log.err(e);
            }
        }
    }

    private static void serverReceive(Player player, String txt) {
        fcPacket[] packets = read(txt);
        //Log.debug("Server recieved, len: " + txt.length());
        for (fcPacket packet : packets) {
            //Log.debug(packet.getClass().getName());
            for (Annotation annotation : packet.getClass().getAnnotations())
                if (annotation instanceof CallFrom cf) if (cf.value() == PacketSource.server) return;
            
            try {
                packet.handleServer(player);
            } catch(Exception e) {
                Log.err(e);
            }
        }
    }

    public static void register() {
        if (Vars.netClient != null) Vars.netClient.addPacketHandler(type, fcNet::clientReceive);
        if (Vars.netServer != null) Vars.netServer.addPacketHandler(type, fcNet::serverReceive);
        Core.app.addListener(new fcNet());
        fcCall.register();
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

    private static byte[] str2Byte(String txt) {
        return txt.getBytes(StandardCharsets.ISO_8859_1);
    }

    private static String byte2Str(byte[] data) {
        return new String(data, StandardCharsets.ISO_8859_1);
    }

    private static byte[] packPackets(Seq<byte[]> lst) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Writes writes = new Writes(new DataOutputStream(stream));
        writes.i(lst.size);
        for (byte[] p : lst) writes.b(p);
        writes.close();
        return stream.toByteArray();
    }

    @Override
    public void update() {
        if (!Vars.net.active()) return;

        if (reliableQueue.size > 0) {
            String data = byte2Str(packPackets(reliableQueue));
            if (Vars.net.server()) {
                Call.clientPacketReliable(type, data);
            } else {
                Call.serverPacketReliable(type, data);
            }
            reliableQueue.clear();
        }
        if (unreliableQueue.size > 0) {
            String data = byte2Str(packPackets(unreliableQueue));
            if (Vars.net.server()) {
                Call.clientPacketUnreliable(type, data);
            } else {
                Call.serverPacketUnreliable(type, data);
            }
            unreliableQueue.clear();
        }
    }
}
