package finalCampaign.launch;

import java.lang.reflect.*;
import java.nio.file.*;
import arc.*;
import arc.files.*;
import finalCampaign.*;
import mindustry.*;

public class dex2jar {
    private static boolean inited = false;
    private static Method from;
    private static Method to;

    private Object instance;

    public static void init() {
        if (inited) return;
        Fi cacheDir = finalCampaign.dataDir.child("dex2jar");
        if (cacheDir.exists()) cacheDir.deleteDirectory();
        cacheDir.mkdirs();

        Fi libPath = finalCampaign.thisModFi.child("class");
        Fi dex2jarFiSrc = Core.app.getVersion() >= 24 ? libPath.child("dex2jar.24.jar") : libPath.child("dex2jar.14.jar");
        Fi dex2jarFi = cacheDir.child("dex2jar.jar");
        dex2jarFiSrc.copyTo(dex2jarFi);
        
        try {
            ClassLoader dex2jarLoader = Vars.platform.loadJar(dex2jarFi, installer.class.getClassLoader());
            Class<?> dex2jar = Class.forName("com.googlecode.d2j.dex.Dex2jar", true, dex2jarLoader);

            from = dex2jar.getDeclaredMethod("from", byte[].class);
            to = dex2jar.getDeclaredMethod("to", Path.class);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public dex2jar(Object o) {
        instance = o;
    }

    public static dex2jar from(byte[] bytecode) {
        try {
            return new dex2jar(from.invoke(null, (Object) bytecode));
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void to(Path path) {
        try {
            to.invoke(instance, path);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}