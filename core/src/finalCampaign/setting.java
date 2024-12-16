package finalCampaign;

import arc.*;
import arc.struct.*;
import arc.struct.ObjectMap.*;
import arc.func.*;

public class setting {
    private static final String prefix = "finalCampaign.";

    private static String processName(String name) {
        if (name.startsWith(prefix)) return name;
        return prefix + name;
    }

    public static boolean has(String name) {
        return Core.settings.has(processName(name));
    }

    public static void clear() {
        Seq<String> shouldBeRemoved = new Seq<>();
        Core.settings.keys().forEach(key -> {
            if (key.startsWith(prefix)) shouldBeRemoved.add(key);
        });
        for (String key : shouldBeRemoved) Core.settings.remove(key);
    }

    public static void remove(String name) {
        Core.settings.remove(processName(name));
    }

    public static void putJson(String name, Object obj) {
        Core.settings.putJson(processName(name), obj);
    }

    public static void putJson(String name, Class<?> elementType, Object obj) {
        Core.settings.putJson(processName(name), elementType, obj);
    }

    public static void put(String name, Object value) {
        Core.settings.put(processName(name), value);
    }

    public static void putAll(ObjectMap<String, Object> map){
        for(Entry<String, Object> entry : map.entries()){
            put(processName(entry.key), entry.value);
        }
    }

    public static Object get(String name, Object def) {
        return Core.settings.get(processName(name), def);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getAndCast(String name, T def) {
        return (T) get(name, def);
    }

    @SuppressWarnings("rawtypes")
    public static <T> T getJson(String name, Class<T> type, Class elementType, Prov<T> def) {
        return Core.settings.getJson(processName(name), type, elementType, def);
    }

    public static <T> T getJson(String name, Class<T> type, Prov<T> def){
        return getJson(name, type, null, def);
    }

}
