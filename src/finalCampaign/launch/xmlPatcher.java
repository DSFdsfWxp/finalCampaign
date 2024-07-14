package finalCampaign.launch;

import java.io.*;
import java.util.*;

public class xmlPatcher {
    private final int magic = 0x00080003;
    private final int StringChunkType = 0x001c0001;

    private byte[] bin;
    private byte[] originalContent;

    private String[] stringMap;
    private String[] styleMap;

    public xmlPatcher(byte[] src) {
        bin = src;
        parse();
    }

    private void parse() {
        littleEndianDataInputStream stream = new littleEndianDataInputStream(bin);
        if (stream.readInt() != magic) throw new RuntimeException("Not a valid AndroidManifest.xml");
        stream.skipBytes(4);

        if (stream.readInt() != StringChunkType) throw new RuntimeException("No String Chunk found in AndroidManifest.xml");
        int chunkSize = stream.readInt();

        byte[] stringChunk = Arrays.copyOfRange(bin, 8, chunkSize + 8);
        originalContent = Arrays.copyOfRange(bin, 8 + chunkSize, bin.length);

        int stringCount = stream.readInt();
        int styleCount = stream.readInt();
        stream.skipBytes(4);
        int stringPoolOffset = stream.readInt();
        int stylePoolOffset = stream.readInt();

        stringMap = new String[stringCount];
        styleMap = new String[styleCount];

        stream = new littleEndianDataInputStream(stringChunk);
        stream.skipBytes(stringPoolOffset);

        for (int i=0; i<stringCount; i++) stringMap[i] = stream.readUTF(true, 2, 2);

        if (styleCount > 0) {
            stream = new littleEndianDataInputStream(stringChunk);
            stream.skipBytes(stylePoolOffset);

            for (int i=0; i<styleCount; i++) styleMap[i] = stream.readUTF(true, 2, 2);
        }
    }

    public void replaceString(String find, String replacement) {
        for (int i=0; i<stringMap.length; i++) stringMap[i] = stringMap[i].replace(find, replacement);
    }

    public void replaceStyle(String find, String replacement) {
        for (int i=0; i<styleMap.length; i++) styleMap[i] = styleMap[i].replace(find, replacement);
    }

    private byte[] buildPackage(int magic, byte[] content) {
        littleEndianDataOutputStream stream = new littleEndianDataOutputStream();
        stream.writeInt(magic);
        stream.writeInt(content.length + 8);
        stream.write(content);
        return stream.toByteArray();
    }

    public byte[] build() {
        littleEndianDataOutputStream stream = new littleEndianDataOutputStream();

        try {
            byte[] stringOffsets;

            int offset = 0;
            for (int i=0; i<stringMap.length; i++) {
                stream.writeInt(offset);
                offset += stringMap[i].length() * 2 + 4;
            }

            stringOffsets = stream.toByteArray();

            stream = new littleEndianDataOutputStream();

            stream.writeInt(stringMap.length);
            stream.writeInt(styleMap.length);

            stream.writeInt(0);

            int stringPoolOffset = 4 * 7 + stringMap.length * 4 + styleMap.length * 4;
            int stylePoolOffset = styleMap.length > 0 ? stringPoolOffset + offset : 0;
            stream.writeInt(stringPoolOffset);
            stream.writeInt(stylePoolOffset);

            stream.write(stringOffsets);

            offset = 0;
            for (int i=0; i<styleMap.length; i++) {
                stream.writeInt(offset);
                offset += styleMap[i].length() * 2 + 4;
            }

            littleEndianDataOutputStream stringStream = new littleEndianDataOutputStream();

            for (int i=0; i<stringMap.length; i++) stringStream.writeUTF(stringMap[i], true, 2, 2);
            for (int i=0; i<styleMap.length; i++) stringStream.writeUTF(styleMap[i], true, 2, 2);

            stringStream.align(4);
            stream.write(stringStream.toByteArray());

            byte[] stringChunk = buildPackage(StringChunkType, stream.toByteArray());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(stringChunk);
            outputStream.write(originalContent);

            return buildPackage(magic, outputStream.toByteArray());
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
