package finalCampaign.patch;

import java.io.*;
import arc.struct.*;
import arc.files.*;
import finalCampaign.*;
import javassist.*;

@SuppressWarnings("rawtypes")
public class pool {
    protected static ClassPool classPool = ClassPool.getDefault();
    protected static Loader.Simple classLoader = new Loader.Simple(classPool.getClass().getClassLoader());

    private static ObjectMap<String, Object> patchedClassMap = new ObjectMap<>();
    private static ObjectMap<String, byte[]> patchedByteCodeMap = new ObjectMap<>();
    private static boolean inited = false;

    public static void init() throws NotFoundException {
        if (inited) return;

        Fi classFile = finalCampaign.dataDir.child("mindustry.class.jar");
        if (!classFile.exists()) {
            ZipFi thisModFi = new ZipFi(finalCampaign.thisMod.file);
            thisModFi.child("mindustry.class.jar").copyTo(classFile);
        }

        classPool.appendClassPath(finalCampaign.thisMod.file.absolutePath());
        classPool.appendClassPath(classFile.absolutePath());

        inited = true;
    }

    public static void cache(Class patchClass, Object patchedClass, byte[] byteCode) {
        cache(patchClass.getName(), patchedClass, byteCode);
    }

    protected static void cache(String patchClassName, Object patchedClass, byte[] byteCode) {
        patchedClassMap.put(patchClassName, patchedClass);
        patchedByteCodeMap.put(patchClassName, byteCode);
    }

    public static boolean has(Class patchClass) {
        return patchedClassMap.containsKey(patchClass.getName()) && patchedByteCodeMap.containsKey(patchClass.getName());
    }

    public static <T> void patchAndCache(Class<T> patchClass) throws NotFoundException, ClassNotFoundException, CannotCompileException, IOException {
        // since we'll load the patched target class before we make a proxy for it
        // so we cache it there
        modify.patch(patchClass);
    }

    public static Class resolve(Class patchClass) {
        if (!has(patchClass)) return null;
        return (Class) patchedClassMap.get(patchClass.getName());
    }

    public static CtClass resolveCtClass(Class patchClass) throws IOException, CannotCompileException {
        if (!has(patchClass)) return null;
        InputStream stream = new ByteArrayInputStream(patchedByteCodeMap.get(patchClass.getName()));
        return classPool.makeClass(stream);
    }

}
