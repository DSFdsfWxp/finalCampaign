package finalCampaign.patch;

import java.io.*;
import arc.struct.*;
import arc.files.*;
import finalCampaign.*;
import javassist.*;

import static mindustry.Vars.*;

public class pool {
    protected static ClassPool classPool = ClassPool.getDefault();
    protected static Loader.Simple classLoader = new Loader.Simple();

    private static ObjectMap<Object, Object> patchedClassMap = new ObjectMap<>();
    private static ObjectMap<Object, byte[]> patchedByteCodeMap = new ObjectMap<>();
    private static boolean inited = false;

    public static void init() throws NotFoundException {
        if (inited) return;

        Fi dataDir = dataDirectory.child("finalCampaign");
        if (!dataDir.exists()) dataDir.mkdirs();

        Fi classFile = dataDir.child("mindustry.class.jar");
        if (!classFile.exists()) {
            ZipFi thisModFi = new ZipFi(finalCampaign.thisMod.file);
            thisModFi.child("mindustry.class.jar").copyTo(classFile);
        }

        classPool.appendClassPath(finalCampaign.thisMod.file.absolutePath());
        classPool.appendClassPath(classFile.absolutePath());

        inited = true;
    }

    public static void cache(Object patchClass, Object patchedClass, byte[] byteCode) {
        patchedClassMap.put(patchClass, patchedClass);
        patchedByteCodeMap.put(patchClass, byteCode);
    }

    public static boolean has(Object patchClass) {
        return patchedClassMap.containsKey(patchClass) && patchedByteCodeMap.containsKey(patchClass);
    }

    public static <T> void patchAndCache(Class<T> patchClass) throws NotFoundException, ClassNotFoundException, CannotCompileException, IOException {
        modify.patch(patchClass);
    }

    @SuppressWarnings("rawtypes")
    public static Class resolve(Object patchClass) {
        if (!has(patchClass)) return null;
        return (Class) patchedClassMap.get(patchClass);
    }

    public static CtClass resolveCtClass(Object patchClass) throws IOException, CannotCompileException {
        if (!has(patchClass)) return null;
        InputStream stream = new ByteArrayInputStream(patchedByteCodeMap.get(patchClass));
        return classPool.makeClass(stream);
    }

}
