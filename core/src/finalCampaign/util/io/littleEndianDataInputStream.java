package finalCampaign.util.io;

import java.io.*;

public class littleEndianDataInputStream {
    private DataInputStream stream;
    private byte[] bin;
    private int pos;

    public littleEndianDataInputStream(byte[] data) {
        stream = new DataInputStream(new ByteArrayInputStream(data));
        bin = data;
        pos = 0;
    }

    public int read() {
        try {
            pos ++;
            return stream.readUnsignedByte();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void skipBytes(int n) {
        try {
            stream.skipBytes(n);
            pos += n;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int readInt() {
        int c1 = read();
        int c2 = read();
        int c3 = read();
        int c4 = read();
        if ((c1 | c2 | c3 | c4) < 0)
            throw new RuntimeException(new EOFException());
        return ((c4 << 24) + (c3 << 16) + (c2 << 8) + (c1 << 0));
    }

    public short readShort() {
        int c1 = read();
        int c2 = read();
        if ((c1 | c2) < 0)
            throw new RuntimeException(new EOFException());
        return (short) ((c2 << 8) + (c1 << 0));
    }

    public byte[] readNBytes(int n) {
        byte[] out = new byte[n];
        try {
            stream.readFully(out);
            pos += n;
            return out;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String readUTF(boolean unicode, int blankEndingLen, int sizeLen) {
        try {
            int len = 0;
            byte c1 = bin[pos];
            byte c2 = bin[pos + 1];

            switch (sizeLen) {
                case 1:
                    len = bin[pos];
                    break;
                case 2: {
                    if ((c1 | c2) < 0)
                        throw new RuntimeException(new EOFException());
                    len = ((c2 << 8) + (c1 << 0));
                    break;
                }
                default:
                    throw new RuntimeException("sizeLen is not supported: " + sizeLen);
            }
            
            String res = null;

            if (unicode) {
                skipBytes(2);
                byte[] txt = readNBytes(len * 2);
                char[] chars = new char[len];
                for (int i=0; i<txt.length; i+=2) {
                    int ch1 = txt[i];
                    int ch2 = txt[i+1];
                    chars[i/2] = (char) ((ch2 << 8) + (ch1 << 0));
                }
                res = new String(chars);
            } else {
                bin[pos] = sizeLen == 1 ? 0 : c2;
                bin[pos + 1] = c1;
                stream = new DataInputStream(new ByteArrayInputStream(bin));
                stream.skipBytes(pos);
                res = stream.readUTF();
                bin[pos] = c1;
                bin[pos + 1] = c2;
                pos += len + 2;
            }
            
            skipBytes(blankEndingLen);
            return res;
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String readUTFWithFixedLength(int len, boolean unicode) {
        byte[] txt = readNBytes(len);
        String res = null;
        int realLen = txt.length;

        boolean found = false;
        for (int i=0; i<txt.length; i++) {
            if (found) {
                if (txt[i] == 0) {
                    realLen = i - 1;
                    break;
                } else {
                    found = false;
                }
            }
            if (txt[i] == 0) found = true;
        }

        if (unicode) {
            if (realLen % 2 != 0) realLen ++;
            char[] chars = new char[realLen / 2];
            for (int i=0; i<realLen; i+=2) {
                int ch1 = txt[i];
                int ch2 = txt[i+1];
                chars[i/2] = (char) ((ch2 << 8) + (ch1 << 0));
            }
            res = new String(chars);
        } else {
            byte[] wrapper = new byte[realLen + 2];
            wrapper[0] = (byte)(realLen >>>  8);
            wrapper[1] = (byte)(realLen >>>  0);
            for (int i=0; i<realLen; i++) wrapper[i + 2] = txt[i];
            DataInputStream s = new DataInputStream(new ByteArrayInputStream(wrapper));
            try {
                res = s.readUTF();
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }

        return res;
    }

    public byte[] readAllAvailabled() {
        return readNBytes(available());
    }

    public int available() {
        return bin.length - pos;
    }

    public int length() {
        return bin.length;
    }

    public int currentPos() {
        return pos;
    }
}
