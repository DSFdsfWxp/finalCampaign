package finalCampaign.launch;

import java.io.*;
import java.lang.reflect.*;
import arc.*;
import arc.files.*;
import finalCampaign.*;
import mindustry.*;

public class apkSigner {
    private static boolean inited = false;
    private static Fi cacheDir;
    private static String certPath;
    private static String keyPath;
    private static Method main;

    public static void init() {
        if (inited) return;

        Fi libPath = finalCampaign.thisModFi.child("class");
        Fi jarSrc = Core.app.getVersion() >= 24 ? libPath.child("apksigner.24.jar") : libPath.child("apksigner.14.jar");
        Fi cert = finalCampaign.thisModFi.child("cert").child("cert");
        cacheDir = finalCampaign.dataDir.child("apkSigner");
        if (cacheDir.exists()) cacheDir.deleteDirectory();
        cacheDir.mkdirs();
        Fi jar = cacheDir.child("apksigner.jar");
        jarSrc.copyTo(jar);

        Fi outCert = cacheDir.child("cert.pem");
        Fi outKey = cacheDir.child("key.pk8");


        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(cert.readBytes());
            DataInputStream stream = new DataInputStream(byteStream);
            
            if (outCert.exists()) outCert.delete();
            if (outKey.exists()) outKey.delete();

            outCert.writeString(stream.readUTF());
            int keyLen = stream.readInt();
            byte[] key = new byte[keyLen];
            stream.readFully(key);
            outKey.writeBytes(key);

            certPath = outCert.absolutePath();
            keyPath = outKey.absolutePath();
            
            stream.close();

            ClassLoader classLoader = Vars.platform.loadJar(jar, apkSigner.class.getClassLoader());
            main = classLoader.loadClass("com.android.apksigner.ApkSignerTool").getDeclaredMethod("main", String[].class);

            inited = true;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Fi sign(Fi apk) {
        Fi signed = cacheDir.child("signed.apk");
        if (signed.exists()) signed.delete();

        String[] args = new String[] {
            "sign",
            "--cert", certPath,
            "--key", keyPath,
            "--v4-signing-enabled", "false",
            "--min-sdk-version", "14",
            "--out", signed.absolutePath(),
            apk.absolutePath()
        };

        try {
            main.invoke(null, (Object) args);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return signed;
    }
}
