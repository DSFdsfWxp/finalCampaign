package finalCampaign.launch;

import java.io.*;
import arc.struct.*;
import arc.util.io.*;

public class bothConfigUtil {
    public static class config {
        public String gameJarName;
        public String dataDir;
        public boolean isServer;
    }

    public static config read(Reader file) {
        config res = new config();
        try {
            ObjectMap<String, String> map = new ObjectMap<>();
            PropertiesUtils.load(map, file);
            res.gameJarName = map.get("gameJarName", "");
            res.dataDir = map.get("dataDir", "");
            res.isServer = map.get("isServer", "").equalsIgnoreCase("true");
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally {
            Streams.close(file);
        }
        return res;
    }

    public static void write(config src, Writer file) {
        try {
            ObjectMap<String, String> map = new ObjectMap<>();
            map.put("gameJarName", src.gameJarName);
            map.put("dataDir", src.dataDir);
            map.put("isServer", src.isServer ? "true" : "false");
            PropertiesUtils.store(map, file, "FinalCampaign Mod Launcher Config File");
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally {
            Streams.close(file);
        }
    }
}
