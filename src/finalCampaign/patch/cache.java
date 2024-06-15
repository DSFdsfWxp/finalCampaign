package finalCampaign.patch;

import java.io.*;
import java.security.*;
import arc.files.*;
import finalCampaign.*;
import javassist.CtClass;

import static mindustry.Vars.*;

public class cache {

    private static boolean inited = false;
    private static Fi cacheDir;

    public static void init() {
        if (inited) return;

        cacheDir = finalCampaign.dataDir.child("patchCache");
        if (!cacheDir.exists()) cacheDir.mkdirs();

        inited = true;
    }

    protected static void write(String type, String patchClassShortHashName, String targetClassName, byte[] data, byte[] dexData) throws NoSuchAlgorithmException {
        Fi path = cacheDir.child(type);
        if (patchClassShortHashName != null) path = path.child(patchClassShortHashName);
        String fileBaseName = util.sha256Hash(targetClassName);
        Fi file = path.child(fileBaseName + "-raw");
        Fi dexFile = path.child(fileBaseName);

        if (!path.exists()) path.mkdirs();
        if (file.exists()) file.delete();

        file.writeBytes(data);

        if (dexData != null) {
            if (dexFile.exists()) dexFile.delete();
            dexFile.writeBytes(dexData);
        }
    }

    public static boolean has(String type, String patchClassShortHashName, String targetClassName) throws NoSuchAlgorithmException {
        Fi path = cacheDir.child(type);
        if (patchClassShortHashName != null) path = path.child(patchClassShortHashName);
        Fi file = path.child(util.sha256Hash(targetClassName) + "-raw");

        if (!path.exists()) return false;
        return file.exists();
    }

    protected static Class<?> resolve(String type, String patchClassShortHashName, String targetClassName) throws NoSuchAlgorithmException, Exception {
        Fi path = cacheDir.child(type);
        if (patchClassShortHashName != null) path = path.child(patchClassShortHashName);
        String fileBaseName = util.sha256Hash(targetClassName);
        Fi file = path.child(android ? fileBaseName : fileBaseName + "-raw");

        if (!file.exists()) throw new RuntimeException("Not cache file for: " + type + " :: " + patchClassShortHashName + " -> " + targetClassName);
        String name = "finalCampaign.patch." + type + "." + (patchClassShortHashName == null ? "" : patchClassShortHashName + ".") + targetClassName;

        if (!android) return pool.loadClassBinary(name, file.readBytes());
        return pool.loadDexFile(name, file);
    }

    protected static CtClass resolveCtClass(String type, String patchClassShortHashName, String targetClassName) throws NoSuchAlgorithmException, IOException {
        Fi path = cacheDir.child(type);
        if (patchClassShortHashName != null) path = path.child(patchClassShortHashName);
        Fi file = path.child(util.sha256Hash(targetClassName) + "-raw");

        if (!file.exists()) throw new RuntimeException("Not cache file for: " + type + " :: " + patchClassShortHashName + " -> " + targetClassName);
        return pool.makeCtClass(file.read());
    }
}
