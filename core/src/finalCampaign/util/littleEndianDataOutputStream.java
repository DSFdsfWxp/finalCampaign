package finalCampaign.util;

import java.io.*;

public class littleEndianDataOutputStream {
    private ByteArrayOutputStream output;
    private DataOutputStream stream;
    private byte[] writeBuffer;
    private int pos;

    public littleEndianDataOutputStream() {
        output = new ByteArrayOutputStream();
        stream = new DataOutputStream(output);
        writeBuffer = new byte[4];
        pos = 0;
    }

    public void write(int aByte) {
        try {
            stream.writeByte(aByte);
            pos ++;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void writeInt(int v) {
        writeBuffer[3] = (byte)(v >>> 24);
        writeBuffer[2] = (byte)(v >>> 16);
        writeBuffer[1] = (byte)(v >>>  8);
        writeBuffer[0] = (byte)(v >>>  0);
        write(writeBuffer, 0, 4);
    }

    public void writeShort(int v) {
        writeBuffer[1] = (byte)(v >>>  8);
        writeBuffer[0] = (byte)(v >>>  0);
        write(writeBuffer, 0, 2);
    }

    public void write(byte[] arr) {
        write(arr, 0, arr.length);
    }

    public void write(byte[] arr, int start, int len) {
        try {
            stream.write(arr, 0, len);
            pos += len;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void writeUTF(String str, boolean unicode, int blankEndingLen, int sizeLen) {
        if ( sizeLen < 1 || sizeLen > 2) throw new RuntimeException("sizeLen is not supported: " + sizeLen);
        try {
            if (unicode) {
                byte[] txt = new byte[str.length() * 2];
                char c;
                int j;
                for (int i=0; i<str.length(); i++) {
                    c = str.charAt(i);
                    j = (c & 0xFF);
                    txt[2 * i] = (byte) j;
                    j = (c >>> 8);
                    txt[2 * i + 1] = (byte) j;
                }
                if (sizeLen == 1) {
                    write(str.length());
                    write(str.length());
                } else {
                    writeShort(str.length());
                }
                write(txt);
            } else {
                ByteArrayOutputStream o = new ByteArrayOutputStream();
                DataOutputStream s = new DataOutputStream(o);
                s.writeUTF(str);
                byte[] txt = o.toByteArray();
                byte c1 = txt[0];
                byte c2 = txt[1];
                txt[0] = c2;
                txt[1] = sizeLen == 1 ? c2 : c1;
                write(txt);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        write(new byte[blankEndingLen]);
    }

    public void writeUTFWithFixedLength(String str, int len, boolean unicode) {
        byte[] txt = new byte[len];

        if (unicode) {
            char c;
            int j;
            for (int i=0; i<str.length(); i++) {
                c = str.charAt(i);
                j = (c & 0xFF);
                txt[2 * i] = (byte) j;
                j = (c >>> 8);
                txt[2 * i + 1] = (byte) j;
            }
        } else {
            try {
                ByteArrayOutputStream o = new ByteArrayOutputStream();
                DataOutputStream s = new DataOutputStream(o);
                s.writeUTF(str);
                byte[] t = o.toByteArray();
                for (int i=2; i<t.length; i++) txt[i - 2] = t[i];
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }

        write(txt);
    }

    public void align(int n) {
        while (length() % n > 0) write(0);
    }

    public byte[] toByteArray() {
        return output.toByteArray();
    }

    public int length() {
        return pos;
    }
}
