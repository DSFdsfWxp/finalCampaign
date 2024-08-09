package finalCampaign.feature.featureClass.buildTargeting;

import arc.struct.*;
import finalCampaign.setting;

public class buildTargetingPreset {
    private static Seq<String> nameLst = new Seq<>();
    private static Seq<byte[]> dataLst = new Seq<>();

    public static void load() {
        int c = setting.getAndCast("buildTargetingPreset.num", 0);
        byte[] def = new byte[4];
        for (int i=0; i<c; i++) {
            String name = setting.getAndCast("buildTargetingPreset." + i + ".name", "Unnamed " + i);
            byte[] data = setting.getAndCast("buildTargetingPreset." + i + ".data", def);
            nameLst.add(name);
            dataLst.add(data);
        }
    }

    public static String getName(int id) {
        return id >= nameLst.size ? "Unnamed " + id : nameLst.get(id);
    }

    public static byte[] getData(int id) {
        byte[] def = new byte[4];
        return id >= dataLst.size ? def : dataLst.get(id);
    }

    public static boolean has(String name) {
        for (String n : nameLst) if (n.trim().equals(name.trim())) return true;
        return false;
    }

    public static int add() {
        int id = nameLst.size;
        String name = getName(id);
        byte[] data = getData(id);
        nameLst.add(name);
        dataLst.add(data);
        put(id, name, data);
        return id;
    }

    public static int size() {
        return Math.max(nameLst.size, dataLst.size);
    }

    public static void rename(int id, String name) {
        nameLst.set(id, name);
        setting.put("buildTargetingPreset." + id + ".name", name);
    }

    public static void remove(int id) {
        if (id >= nameLst.size) return;
        nameLst.remove(id);
        dataLst.remove(id);
        setting.remove("buildTargetingPreset." + id + ".name");
        setting.remove("buildTargetingPreset." + id + ".data");
    }

    public static void put(int id, String name, byte[] data) {
        nameLst.set(id, name);
        dataLst.set(id, data);
        setting.put("buildTargetingPreset." + id + ".name", name);
        setting.put("buildTargetingPreset." + id + ".data", data);
    }
}
