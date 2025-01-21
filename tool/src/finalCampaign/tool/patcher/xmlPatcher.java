package finalCampaign.tool.patcher;

import java.util.*;
import arc.struct.*;
import finalCampaign.tool.io.*;

public class xmlPatcher {
    private static final int magic = 0x00080003;
    public static final int StringChunkType = 0x001c0001;
    public static final int ResourceIdChunkType = 0x00080180;
    public static final int StartNamespaceChunkType = 0x00100100;
    public static final int EndNamespaceChunkType = 0x00100101;
    public static final int StartTagChunkType = 0x00100102;
    public static final int EndTagChunkType = 0x00100103;
    public static final int TextChunkType = 0x00100104;

    public static final int AttrStringType = 0x03000008;

    public static class basePackage {
        int magic;
        int length;
        byte[] data;

        public basePackage(byte[] bin) {
            littleEndianDataInputStream stream = new littleEndianDataInputStream(bin);
            magic = stream.readInt();
            length = stream.readInt();
            data = stream.readNBytes(length - 8);
        }

        public byte[] build() {
            littleEndianDataOutputStream stream = new littleEndianDataOutputStream();
            stream.writeInt(magic);
            stream.writeInt(data.length + 8);
            stream.write(data);
            return stream.toByteArray();
        }
    }

    public static class stringChunk extends basePackage {
        Seq<String> strings;
        Seq<String> styles;

        public stringChunk(byte[] bin) {
            super(bin);
            if (magic != StringChunkType) throw new RuntimeException("Not a valid string chunk.");
            littleEndianDataInputStream stream = new littleEndianDataInputStream(data);

            int stringCount = stream.readInt();
            int styleCount = stream.readInt();

            stream.skipBytes(4 * (stringCount + styleCount + 3));

            String[] strings = new String[stringCount];
            String[] styles = new String[styleCount];

            for (int i=0; i<stringCount; i++) strings[i] = stream.readUTF(true, 2, 2);
            for (int i=0; i<styleCount; i++) styles[i] = stream.readUTF(true, 2, 2);

            this.strings = new Seq<>(strings);
            this.styles = new Seq<>(styles);
        }

        public byte[] build() {
            littleEndianDataOutputStream stream = new littleEndianDataOutputStream();
            littleEndianDataOutputStream offsetsStream = new littleEndianDataOutputStream();
            littleEndianDataOutputStream poolStream = new littleEndianDataOutputStream();

            for (String s : strings) {
                offsetsStream.writeInt(poolStream.length());
                poolStream.writeUTF(s, true, 2, 2);
            }

            int offset = poolStream.length();
            for (String s : styles) {
                offsetsStream.writeInt(poolStream.length() - offset);
                poolStream.writeUTF(s, true, 2, 2);
            }

            poolStream.align(4);

            stream.writeInt(strings.size);
            stream.writeInt(styles.size);

            stream.writeInt(0);

            stream.writeInt(4 * (strings.size + styles.size + 7));
            stream.writeInt(styles.size == 0 ? 0 : (4 * (strings.size + styles.size + 7) + offset));

            stream.write(offsetsStream.toByteArray());
            stream.write(poolStream.toByteArray());

            data = stream.toByteArray();
            return super.build();
        }

        public int locateOrAddString(String str) {
            int pos = strings.indexOf(str);
            if (pos == -1) {
                pos = strings.size;
                strings.add(str);
            }
            return pos;
        }
    }

    public static class resourceIdChunk extends basePackage {
        Seq<Integer> ids;

        public resourceIdChunk(byte[] bin) {
            super(bin);
            if (magic != ResourceIdChunkType) throw new RuntimeException("Not a valid resource id chunk.");
            littleEndianDataInputStream stream = new littleEndianDataInputStream(data);

            int num = stream.length() / 4;
            Integer[] ids = new Integer[num];

            for (int i=0; i<num; i++) ids[i] = stream.readInt();

            this.ids = new Seq<>(ids);
        }

        public byte[] build() {
            littleEndianDataOutputStream stream = new littleEndianDataOutputStream();

            for (Integer i : ids) stream.writeInt(i);

            data = stream.toByteArray();
            return super.build();
        }
    }

    public static class xmlContentChunk extends basePackage {
        int lineNumber;
        byte[] xmlData;

        public xmlContentChunk(byte[] bin) {
            super(bin);
            littleEndianDataInputStream stream = new littleEndianDataInputStream(data);

            lineNumber = stream.readInt();

            stream.skipBytes(4);

            xmlData = stream.readAllAvailabled();
        }

        public byte[] build() {
            littleEndianDataOutputStream stream = new littleEndianDataOutputStream();

            stream.writeInt(lineNumber);

            stream.writeInt(0xffffffff);

            stream.write(xmlData);

            data = stream.toByteArray();
            return super.build();
        }
    }

    public static class startNamespaceChunk extends xmlContentChunk {
        int prefix;
        int uri;

        public startNamespaceChunk(byte[] bin) {
            super(bin);
            if (magic != StartNamespaceChunkType) throw new RuntimeException("Not a valid start namespace chunk.");
            littleEndianDataInputStream stream = new littleEndianDataInputStream(xmlData);

            prefix = stream.readInt();
            uri = stream.readInt();
        }

        public byte[] build() {
            littleEndianDataOutputStream stream = new littleEndianDataOutputStream();

            stream.writeInt(prefix);
            stream.writeInt(uri);

            xmlData = stream.toByteArray();
            return super.build();
        }
    }

    public static class endNamespaceChunk extends xmlContentChunk {
        int prefix;
        int uri;

        public endNamespaceChunk(byte[] bin) {
            super(bin);
            if (magic != EndNamespaceChunkType) throw new RuntimeException("Not a valid end namespace chunk.");
            littleEndianDataInputStream stream = new littleEndianDataInputStream(xmlData);

            prefix = stream.readInt();
            uri = stream.readInt();
        }

        public byte[] build() {
            littleEndianDataOutputStream stream = new littleEndianDataOutputStream();

            stream.writeInt(prefix);
            stream.writeInt(uri);

            xmlData = stream.toByteArray();
            return super.build();
        }
    }

    public class tagAttribute {
        public int namespaceUri;
        public int name;
        public int valueStr;
        public int type;
        public int data;

        public String readAsString() {
            if (type != AttrStringType) throw new RuntimeException("Not a string type attribute: " + this);
            return manifest.string.strings.get(valueStr);
        }

        public String name() {
            return manifest.string.strings.get(name);
        }
    }

    public class startTagChunk extends xmlContentChunk {
        int namespaceUri;
        int name;
        int flag;
        int classAttribute;
        Seq<tagAttribute> attributes;

        public startTagChunk(byte[] bin) {
            super(bin);
            if (magic != StartTagChunkType) throw new RuntimeException("Not a valid start tag chunk.");
            littleEndianDataInputStream stream = new littleEndianDataInputStream(xmlData);

            namespaceUri = stream.readInt();
            name = stream.readInt();
            flag = stream.readInt();

            int attributeNum = stream.readInt();

            classAttribute = stream.readInt();
            
            tagAttribute[] attributes = new tagAttribute[attributeNum];
            for (int i=0; i<attributeNum; i++) {
                tagAttribute attribute = new tagAttribute();
                attribute.namespaceUri = stream.readInt();
                attribute.name = stream.readInt();
                attribute.valueStr = stream.readInt();
                attribute.type = stream.readInt();
                attribute.data = stream.readInt();
                attributes[i] = attribute;
            }

            this.attributes = new Seq<>(attributes);
        }

        public byte[] build() {
            littleEndianDataOutputStream stream = new littleEndianDataOutputStream();

            stream.writeInt(namespaceUri);
            stream.writeInt(name);
            stream.writeInt(flag);
            stream.writeInt(attributes.size);
            stream.writeInt(classAttribute);

            for (tagAttribute attribute : attributes) {
                stream.writeInt(attribute.namespaceUri);
                stream.writeInt(attribute.name);
                stream.writeInt(attribute.valueStr);
                stream.writeInt(attribute.type);
                stream.writeInt(attribute.data);
            }

            xmlData = stream.toByteArray();
            return super.build();
        }

        public String name() {
            return manifest.string.strings.get(name);
        }
    }

    public class endTagChunk extends xmlContentChunk {
        int namespaceUri;
        int name;

        public endTagChunk(byte[] bin) {
            super(bin);
            if (magic != EndTagChunkType) throw new RuntimeException("Not a valid end tag chunk.");
            littleEndianDataInputStream stream = new littleEndianDataInputStream(xmlData);

            namespaceUri = stream.readInt();
            name = stream.readInt();
        }

        public byte[] build() {
            littleEndianDataOutputStream stream = new littleEndianDataOutputStream();

            stream.writeInt(namespaceUri);
            stream.writeInt(name);

            xmlData = stream.toByteArray();
            return super.build();
        }

        public String name() {
            return manifest.string.strings.get(name);
        }
    }

    public class textChunk extends xmlContentChunk {
        int name;
        int unknow1;
        int unknow2;

        public textChunk(byte[] bin) {
            super(bin);
            if (magic != TextChunkType) throw new RuntimeException("Not a valid text chunk.");
            littleEndianDataInputStream stream = new littleEndianDataInputStream(xmlData);

            name = stream.readInt();
            unknow1 = stream.readInt();
            unknow2 = stream.readInt();
        }

        public byte[] build() {
            littleEndianDataOutputStream stream = new littleEndianDataOutputStream();

            stream.writeInt(name);
            stream.writeInt(unknow1);
            stream.writeInt(unknow2);

            xmlData = stream.toByteArray();
            return super.build();
        }

        public String name() {
            return manifest.string.strings.get(name);
        }
    }

    public class androidManifest extends basePackage {
        public stringChunk string;
        public resourceIdChunk resourceId;
        public xmlItem xml;

        public androidManifest(byte[] bin) {
            super(bin);
            if (magic != xmlPatcher.magic) throw new RuntimeException("Not a valid AndroidManifest.xml");
            littleEndianDataInputStream stream = new littleEndianDataInputStream(data);
            
            int offset = 0;
            string = new stringChunk(data);
            offset += string.length;

            stream.skipBytes(offset);
            resourceId = new resourceIdChunk(stream.readAllAvailabled());
            offset += resourceId.length;

            Seq<xmlContentChunk> xmls = new Seq<>();
            while (offset < data.length) {
                stream = new littleEndianDataInputStream(data);
                stream.skipBytes(offset);
                xmlContentChunk xml = new xmlContentChunk(stream.readAllAvailabled());
                offset += xml.length;
                xmls.add(cloneAndParseXmlContentChunk(xml));
            }

            manifest = this;
            xml = new xmlItem(xmls);
        }

        public byte[] build() {
            littleEndianDataOutputStream stream = new littleEndianDataOutputStream();

            stream.write(string.build());
            stream.write(resourceId.build());
            Seq<xmlContentChunk> xmls = xml.build();
            for (xmlContentChunk c : xmls) stream.write(c.build());

            data = stream.toByteArray();
            return super.build();
        }
    }

    public androidManifest manifest;

    public xmlPatcher(byte[] src) {
        manifest = new androidManifest(src);
    }

    public xmlContentChunk cloneAndParseXmlContentChunk(xmlContentChunk base) {
        byte[] d = base.build();
        return switch (base.magic) {
            case StartNamespaceChunkType -> new startNamespaceChunk(d);
            case EndNamespaceChunkType -> new endNamespaceChunk(d);
            case StartTagChunkType -> new startTagChunk(d);
            case EndTagChunkType -> new endTagChunk(d);
            case TextChunkType -> new textChunk(d);
            default -> throw new RuntimeException("Can not parse xml content chunk: " + base);
        };
    }

    public void replaceString(String find, String replacement) {
        for (int i=0; i<manifest.string.strings.size; i++) manifest.string.strings.set(i, manifest.string.strings.get(i).replace(find, replacement));
    }

    public void replaceStyle(String find, String replacement) {
        for (int i=0; i<manifest.string.styles.size; i++) manifest.string.styles.set(i, manifest.string.styles.get(i).replace(find, replacement));
    }

    public byte[] build() {
        return manifest.build();
    }

    public class xmlItem {
        boolean isNamespace;
        xmlContentChunk start;
        xmlContentChunk end;
        public Seq<xmlItem> child;
        public String name;

        public xmlItem(xmlItem src) {
            isNamespace = src.isNamespace;
            start = cloneAndParseXmlContentChunk(src.start);
            if (src.start == src.end) {
                end = start;
            } else {
                end = cloneAndParseXmlContentChunk(src.end);
            }
            name = src.name;
            child = new Seq<>();
            for (xmlItem i : src.child) child.add(i.clone());
        }

        public xmlItem(textChunk src) {
            isNamespace = false;
            start = src;
            end = src;
            child = new Seq<>();
            name = manifest.string.strings.get(src.name);
        }

        public xmlItem(Seq<xmlContentChunk> src) {
            start = src.get(0);
            isNamespace = start instanceof startNamespaceChunk;
            if (!isNamespace) name = ((startTagChunk) start).name();
            this.child = new Seq<>();
            for (int i=1; i<src.size; i++) {
                xmlContentChunk item = src.get(i);
                if (item instanceof startNamespaceChunk || item instanceof startTagChunk) {
                    Seq<xmlContentChunk> r = new Seq<>(Arrays.copyOfRange(src.toArray(xmlContentChunk.class), i, src.size));
                    xmlItem child = new xmlItem(r);
                    i = src.indexOf(child.end, true);
                    this.child.add(child);
                    continue;
                }
                if (item instanceof endNamespaceChunk || item instanceof endTagChunk) {
                    if (item instanceof endNamespaceChunk != isNamespace) throw new RuntimeException("Not expected end of item: " + item);
                    if (!isNamespace && ((startTagChunk) start).name != ((endTagChunk) item).name) throw new RuntimeException("Not expected end of item: " + item);
                    end = item;
                    break;
                }
                child.add(new xmlItem((textChunk) item));
            }
        }

        public tagAttribute findAttribute(String name, int type) {
            for (tagAttribute a : ((startTagChunk) start).attributes) {
                if (manifest.string.strings.get(a.name).equals(name) && a.type == type) return a;
            }
            throw new RuntimeException("Attribute not found: " + name + ", " + type);
        }

        public Seq<xmlContentChunk> build() {
            Seq<xmlContentChunk> res = new Seq<>();
            res.add(start);
            if (start == end) return res;
            for (xmlItem i : child) res.add(i.build());
            res.add(end);
            return res;
        }

        public xmlItem clone() {
            return new xmlItem(this);
        }

        public Seq<xmlItem> selectChild(String name) {
            Seq<xmlItem> res = new Seq<>();
            for (xmlItem c : child) if (c.name != null) if (c.name.equals(name)) res.add(c);
            return res;
        }

        public xmlItem findChild(String name) {
            for (xmlItem c : child) if (c.name != null) if (c.name.equals(name)) return c;
            throw new RuntimeException("Child not found: " + name);
        }
    }
}
