package finalCampaign.util;

import java.io.*;
import arc.util.serialization.*;

public class objectData {
    public static <T> byte[] write(T obj) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        Json json = new Json();

        json.setWriter(new UBJsonWriter(byteStream));
        json.writeValue(obj, obj.getClass());

        return byteStream.toByteArray();
    }

    public static <T> T read(byte[] data, Class<T> type) {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        Json json = new Json();
        UBJsonReader reader = new UBJsonReader();

        return json.readValue(type, reader.parse(byteStream));
    }
}
