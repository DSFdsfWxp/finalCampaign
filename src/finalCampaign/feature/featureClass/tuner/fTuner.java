package finalCampaign.feature.featureClass.tuner;

import arc.struct.*;
import arc.func.*;
import finalCampaign.*;
import mindustry.*;
import mindustry.gen.*;

public class fTuner {
    private static ObjectMap<String, Boolean> map;
    private static ObjectMap<String, ObjectMap<String, Object>> customMap;
    private static tunerPane pane;

    public static void init() {
        map = new ObjectMap<>();
        customMap = new ObjectMap<>();
        Vars.ui.settings.addCategory(bundle.get("tuner.pane.title"), Icon.hammer, t -> {
            pane = new tunerPane(t);
        });
    }

    public static void load() {

    }

    public static void add(String name, boolean def) {
        if (map.containsKey(name)) throw new RuntimeException("Two tuner items with the same name are not allowed: " + name);
        map.put(name, setting.getAndCast(name, def));
        pane.addItem(name, false, null, null);
    }

    public static void add(String name, boolean def, Cons<tunerPane.customBuilder> customCons) {
        if (map.containsKey(name)) throw new RuntimeException("Two tuner items with the same name are not allowed: " + name);
        map.put(name, setting.getAndCast(name, def));
        ObjectMap<String, Object> cMap = new ObjectMap<>();
        customMap.put(name, cMap);
        pane.addItem(name, true, customCons, cMap);
    }

    public static boolean isOn(String name) {
        if (!map.containsKey(name)) throw new RuntimeException("The tuner item not exsited: " + name);
        return map.get(name);
    }

    public static void set(String name, boolean value) {
        if (!map.containsKey(name)) throw new RuntimeException("The tuner item not exsited: " + name);
        map.put(name, value);
        setting.put("tuner." + name, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getCustomValue(String superName, String name, Class<T> type) {
        return (T) customMap.get(superName).get(name);
    }

    public static void setCustomValue(String superName, String name, Object value) {
        if (!map.containsKey(superName)) throw new RuntimeException("The tuner item not exsited: " + superName);
        ObjectMap<String, Object> cMap = customMap.get(superName);
        if (!cMap.containsKey(name)) throw new RuntimeException("The tuner custom item not exsited: " + superName + " -> " + name);
        cMap.put(name, value);
        setting.put("tuner." + superName + "." + name, value);
    }

    protected static void load(String name) {
        map.put(name, (boolean) setting.get("tuner." + name, map.get(name)));
    }

    protected static void loadCustom(String superName, String name, Object def) {
        customMap.get(superName).put(name, setting.get("tuner." + superName + "." + name, def));
    }

}