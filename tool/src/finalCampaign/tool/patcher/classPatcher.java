package finalCampaign.tool.patcher;

import java.io.*;

public class classPatcher {
    private int magic;
    public int version;
    public int constentPoolCount;
    public constentPoolItem[] constentItems;
    public short accessFlags;
    public int classInfoPos;
    public int superClassInfoPos;
    public byte[] otherContent;
    private boolean modified;

    private static final int classMagic = 0xCAFEBABE;

    public static class constentPoolItem {
        public int tag;
        public byte[] data;
        public String string;
        public int pos1;
        public int pos2;
    }

    // parse java class file and apply patch
    public classPatcher(byte[] bin) {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(bin));
        try {
            magic = stream.readInt();

            if (magic != classMagic) throw new RuntimeException("Not a valid class file.");

            version = stream.readInt();
            constentPoolCount = stream.readShort() - 1;

            constentItems = new constentPoolItem[constentPoolCount];

            for (int i=0; i<constentPoolCount; i++) {
                int tag = stream.readUnsignedByte();
                constentPoolItem item = new constentPoolItem();
                constentItems[i] = item;
                item.tag = tag;
                boolean additionalConstentPoolSlotTakeUp = false;

                switch (tag) {
                    case 1: {
                        item.string = stream.readUTF();
                        break;
                    }
                    case 3:
                    case 4:
                    case 17:
                    case 18: {
                        item.data = new byte[4];
                        break;
                    }
                    case 5:
                    case 6: {
                        additionalConstentPoolSlotTakeUp = true;
                        item.data = new byte[8];
                        break;
                    }
                    case 7:
                    case 8: {
                        item.pos1 = stream.readShort() - 1;
                        break;
                    }
                    case 9:
                    case 10:
                    case 11:
                    case 12: {
                        item.pos1 = stream.readShort() - 1;
                        item.pos2 = stream.readShort() - 1;
                        break;
                    }
                    case 15: {
                        item.data = new byte[3];
                        break;
                    }
                    case 16:
                    case 19:
                    case 20: {
                        item.data = new byte[2];
                        break;
                    }
                    default: {
                        throw new RuntimeException("Unknow tag id: " + tag);
                    }
                }

                if (item.data != null) stream.readFully(item.data);

                if (additionalConstentPoolSlotTakeUp) {
                    constentPoolItem placeholder = new constentPoolItem();
                    placeholder.tag = -1;
                    constentItems[i + 1] = placeholder;
                    i ++;
                }
            }

            accessFlags = stream.readShort();
            classInfoPos = stream.readShort() - 1;
            superClassInfoPos = stream.readShort() - 1;

            otherContent = new byte[stream.available()];
            stream.readFully(otherContent);
            modified = false;
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] build() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(outputStream);
        try {
            stream.writeInt(magic);
            stream.writeInt(version);
            stream.writeShort(constentPoolCount + 1);

            for (int i=0; i<constentPoolCount; i++) {
                constentPoolItem item = constentItems[i];
                if (item.tag == -1) continue;
                stream.writeByte(item.tag);

                switch (item.tag) {
                    case 1: {
                        stream.writeUTF(item.string);
                        break;
                    }
                    case 7:
                    case 8: {
                        stream.writeShort(item.pos1 + 1);
                        break;
                    }
                    case 9:
                    case 10:
                    case 11:
                    case 12: {
                        stream.writeShort(item.pos1 + 1);
                        stream.writeShort(item.pos2 + 1);
                        break;
                    }
                }

                if (item.data != null) stream.write(item.data);
                
            }

            stream.writeShort(accessFlags);
            stream.writeShort(classInfoPos + 1);
            stream.writeShort(superClassInfoPos + 1);

            stream.write(otherContent);

            return outputStream.toByteArray();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean modified() {
        return modified;
    }

    public String getClassName() {
        return constentItems[constentItems[classInfoPos].pos1].string;
    }

    public String getSuperClassName() {
        return constentItems[constentItems[superClassInfoPos].pos1].string;
    }

    // the aim of these string replacing methods is to replace the class name back, which is modified by dex2jar for a "lambda name security" reason
    // it just replace "-" to "_" *partly*, yes, not completely
    // such as:
    //   a$-CC -> a$_CC,                                causing system could not find a$-CC for interface default function
    //                                                  (because a$-CC is not directly referred by other class)
    //   -$$Lambda$a$xx-xx_xx -> _$$Lambda$a$xx_xx_xx,  causing class not found exception
    //                                                  (it replaced class name, but not replaced *all* references)
    //
    // but now we patched dex2jar, so these methods are unused.
    private String replace(String src, String find, String replacement) {
        String out = src.replace(find, replacement);
        if (!out.equals(src)) modified = true;
        return out;
    }

    public void replaceString(String find, String replacement, boolean ignoreName, boolean ignoreClass, int ...tags) {
        for (int i=0; i<constentPoolCount; i++) {
            constentPoolItem item1 = null;
            constentPoolItem item2 = null;
            constentPoolItem item3 = null;

            int tag = -1;
            for (int j : tags) if (j == constentItems[i].tag) tag = j;

            switch (tag) {
                case 7: {
                    if (!ignoreClass) item1 = constentItems[constentItems[i].pos1];
                    break;
                }
                case 8: {
                    item1 = constentItems[constentItems[i].pos1];
                    break;
                }
                case 9:
                case 10:
                case 11: {
                    if (!ignoreClass) {
                        item1 = constentItems[constentItems[i].pos1];
                        item1 = constentItems[item1.pos1];
                    }
                    constentPoolItem item = constentItems[constentItems[i].pos2];
                    if (!ignoreName) item2 = constentItems[item.pos1];
                    if (!ignoreClass) item3 = constentItems[item.pos2];
                    break;
                }
                case 12: {
                    if (!ignoreName) item1 = constentItems[constentItems[i].pos1];
                    if (!ignoreClass) item2 = constentItems[constentItems[i].pos2];
                    break;
                }
            }

            if (item1 != null) item1.string = replace(item1.string, find, replacement);
            if (item2 != null) item2.string = replace(item2.string, find, replacement);
            if (item3 != null) item3.string = replace(item3.string, find, replacement);
        }
    }

    public void replaceString(String find, String replacement) {
        for (int i=0; i<constentPoolCount; i++) if (constentItems[i].tag == 1) constentItems[i].string = replace(constentItems[i].string, find, replacement);
    }
}
