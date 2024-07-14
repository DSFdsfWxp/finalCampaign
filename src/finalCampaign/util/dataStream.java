package finalCampaign.util;

import java.io.*;

public class dataStream {
    public static void writeIntArray(DataOutputStream stream, int[] src) throws IOException {
        if (src == null) {
            stream.writeInt(-1);
            return;
        }
        stream.writeInt(src.length);
        for (int i : src) stream.writeInt(i);
    }

    public static int[] readIntArray(DataInputStream stream) throws IOException {
        int length = stream.readInt();
        if (length == -1) return null;
        int[] out = new int[length];
        for (int i=0; i<out.length; i++) out[i] = stream.readInt();
        return out;
    }
}
