package finalCampaign.patch.impl;

import java.io.*;
import org.spongepowered.asm.mixin.*;
import arc.struct.*;
import arc.struct.ObjectMap.*;
import arc.util.*;
import finalCampaign.feature.featureClass.mapVersion.*;
import mindustry.io.*;

@Mixin(SaveFileReader.class)
public class fcSaveFileReader {
    public StringMap readStringMap(DataInput stream) throws IOException {
        StringMap map = new StringMap();
        short size = stream.readShort();
        for(int i = 0; i < size; i++){
            String name = stream.readUTF();
            String value = stream.readUTF();
            map.put(name, value);
            if (name.equals("finalCampaign.mapVersion") && Strings.canParsePositiveInt(value)) fMapVersion.currentVersion(Integer.parseInt(value));
        }
        return map;
    }

    public void writeStringMap(DataOutput stream, ObjectMap<String, String> map) throws IOException {
        stream.writeShort(map.size + 1);
        for(Entry<String, String> entry : map.entries()){
            stream.writeUTF(entry.key);
            stream.writeUTF(entry.value);
        }
        stream.writeUTF("finalCampaign.mapVersion");
        stream.writeUTF(Integer.toString(fMapVersion.version));
    }
}
