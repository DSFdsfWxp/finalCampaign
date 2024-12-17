package finalCampaign.util;

import finalCampaign.util.io.littleEndianDataInputStream;
import finalCampaign.util.io.littleEndianDataOutputStream;

public class arscPatcher {
    private static final short RES_TABLE_TYPE = 0x2;
    private static final short RES_PACKAGE_TABLE_TYPE = 0x200;
    private static final short RES_STRING_POOL_TYPE = 0x1;

    private static class basePackage {
        int magic;
        byte[] headerData;
        byte[] body;

        public basePackage(byte[] bin) {
            littleEndianDataInputStream stream = new littleEndianDataInputStream(bin);
            magic = stream.readShort();
            int headerDataSize = stream.readShort() - 8;
            int bodySize = stream.readInt() - 8 - headerDataSize;
            headerData = stream.readNBytes(headerDataSize);
            body = stream.readNBytes(bodySize);
        }

        public int headerSize() {
            return headerData.length + 8;
        }

        public int size() {
            return headerSize() + body.length;
        }

        public byte[] build() {
            littleEndianDataOutputStream stream = new littleEndianDataOutputStream();
            stream.writeShort(magic);
            stream.writeShort(headerSize());
            stream.writeInt(size());
            stream.write(headerData);
            stream.write(body);
            return stream.toByteArray();
        }
    }

    private static class stringPool extends basePackage {
        int stringCount;
        int styleCount;
        int flag;
        int stringOffset;
        @SuppressWarnings("unused")
        int styleOffset;
        int[] stringOffsets;
        int[] styleOffsets;
        String[] strings;
        String[] styles;

        public stringPool(byte[] bin) {
            super(bin);
            if (magic != RES_STRING_POOL_TYPE) throw new RuntimeException("Not a valid string pool.");
            littleEndianDataInputStream stream = new littleEndianDataInputStream(headerData);

            stringCount = stream.readInt();
            styleCount = stream.readInt();
            flag = stream.readInt();
            stringOffset = stream.readInt();
            styleOffset = stream.readInt();

            stringOffsets = new int[stringCount];
            styleOffsets = new int[styleCount];

            stream = new littleEndianDataInputStream(body);

            for (int i=0; i<stringCount; i++) stringOffsets[i] = stream.readInt();
            for (int i=0; i<styleCount; i++) styleOffsets[i] = stream.readInt();

            strings = new String[stringCount];
            styles = new String[styleCount];

            for (int i=0; i<stringCount; i++) strings[i] = stream.readUTF(false, 1, 1);
            for (int i=0; i<styleCount; i++) styles[i] = stream.readUTF(false, 1, 1);
        }

        @Override
        public byte[] build() {
            littleEndianDataOutputStream stream = new littleEndianDataOutputStream();
            littleEndianDataOutputStream pool = new littleEndianDataOutputStream();
            littleEndianDataOutputStream stringPool = new littleEndianDataOutputStream();
            littleEndianDataOutputStream stylePool = new littleEndianDataOutputStream();

            for (int i=0; i<stringCount; i++) {
                stringOffsets[i] = stringPool.length();
                stringPool.writeUTF(strings[i], false, 1, 1);
            }

            for (int i=0; i<styleCount; i++) {
                styleOffsets[i] = stylePool.length();
                stylePool.writeUTF(styles[i], false, 1, 1);
            }

            pool.write(stringPool.toByteArray());
            pool.write(stylePool.toByteArray());
            pool.align(4);

            stream.writeInt(stringCount);
            stream.writeInt(styleCount);
            stream.writeInt(flag);
            stream.writeInt(stringOffset);
            stream.writeInt(styleCount == 0 ? 0 : stringOffset + stringPool.length());

            headerData = stream.toByteArray();

            stream = new littleEndianDataOutputStream();

            for (int i=0;i<stringCount; i++) stream.writeInt(stringOffsets[i]);
            for (int i=0;i<styleCount; i++) stream.writeInt(styleOffsets[i]);

            stream.write(pool.toByteArray());
            
            body = stream.toByteArray();

            return super.build();
        }
    }

    private static class packPackage extends basePackage {
        int packageId;
        String packageName;
        byte[] restHeaderData;

        public packPackage(byte[] bin) {
            super(bin);
            if (magic != RES_PACKAGE_TABLE_TYPE) throw new RuntimeException("Not a valid package");
            littleEndianDataInputStream stream = new littleEndianDataInputStream(headerData);

            packageId = stream.readInt();
            packageName = stream.readUTFWithFixedLength(256, true);
            restHeaderData = stream.readAllAvailabled();
        }

        @Override
        public byte[] build() {
            littleEndianDataOutputStream stream = new littleEndianDataOutputStream();

            stream.writeInt(packageId);
            stream.writeUTFWithFixedLength(packageName, 256, true);
            stream.write(restHeaderData);
            
            headerData = stream.toByteArray();

            return super.build();
        }
    }

    private static class arscFile extends basePackage {
        int packageNum;
        stringPool pool;
        packPackage[] packages;

        public arscFile(byte[] bin) {
            super(bin);
            if (magic != RES_TABLE_TYPE) throw new RuntimeException("Not a valid resources.arsc");

            littleEndianDataInputStream stream = new littleEndianDataInputStream(headerData);
            packageNum = stream.readInt();
            pool = new stringPool(body);

            packages = new packPackage[packageNum];
            int offset = pool.size();
            for (int i=0; i<packageNum; i++) {
                stream = new littleEndianDataInputStream(body);
                stream.skipBytes(offset);
                packages[i] = new packPackage(stream.readAllAvailabled());
                offset += packages[i].size();
            }
        }

        @Override
        public byte[] build() {
            littleEndianDataOutputStream stream = new littleEndianDataOutputStream();

            stream.writeInt(packageNum);
            headerData = stream.toByteArray();

            stream = new littleEndianDataOutputStream();

            stream.write(pool.build());
            for (int i=0; i<packageNum; i++) stream.write(packages[i].build());

            body = stream.toByteArray();

            return super.build();
        }
    }

    private arscFile file;

    public arscPatcher(byte[] bin) {
        file = new arscFile(bin);
    }

    public void replaceString(String find, String replacement) {
        for (int i=0; i<file.pool.stringCount; i++) file.pool.strings[i] = file.pool.strings[i].replace(find, replacement);
    }

    public void replaceStyle(String find, String replacement) {
        for (int i=0; i<file.pool.styleCount; i++) file.pool.styles[i] = file.pool.styles[i].replace(find, replacement);
    }

    public void replacePackageName(String find, String replacement) {
        for (int i=0; i<file.packageNum; i++) file.packages[i].packageName = file.packages[i].packageName.replace(find, replacement);
    }

    public byte[] build() {
        return file.build();
    }
}
