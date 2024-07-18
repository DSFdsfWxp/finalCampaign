package finalCampaign.launch;

import java.io.*;
import arc.util.io.*;

public class bothConfigUtil {
    public static class config {
        public String appName;
        public String version;
        public String modName;
        public String gameJarName;
        public String dataDir;
    }

    public static config read(InputStream file) {
        config res = new config();
        try {
            DataInputStream stream = new DataInputStream(file);
            res.appName = stream.readUTF();
            res.version = stream.readUTF();
            res.modName = stream.readUTF();
            res.gameJarName = stream.readUTF();
            res.dataDir = stream.readUTF();
            stream.close();
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally {
            Streams.close(file);
        }
        return res;
    }

    public static void write(config src, OutputStream file) {
        try {
            DataOutputStream stream = new DataOutputStream(file);
            stream.writeUTF(src.appName);
            stream.writeUTF(src.version);
            stream.writeUTF(src.modName);
            stream.writeUTF(src.gameJarName);
            stream.writeUTF(src.dataDir);
            stream.close();
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally {
            Streams.close(file);
        }
    }
}
