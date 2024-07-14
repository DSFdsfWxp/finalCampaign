package finalCampaign.launch;

import java.io.*;
import arc.files.*;

public class bothConfigUtil {
    public static class config {
        public String appName;
        public String version;
        public String modName;
        public String gameJarName;
    }

    public static config read(Fi file) {
        config res = new config();
        try {
            DataInputStream stream = new DataInputStream(file.read());
            res.appName = stream.readUTF();
            res.version = stream.readUTF();
            res.modName = stream.readUTF();
            res.gameJarName = stream.readUTF();
            stream.close();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    public static void write(config src, Fi file) {
        try {
            if (file.exists()) file.delete();
            DataOutputStream stream = new DataOutputStream(file.write());
            stream.writeUTF(src.appName);
            stream.writeUTF(src.version);
            stream.writeUTF(src.modName);
            stream.writeUTF(src.gameJarName);
            stream.close();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
