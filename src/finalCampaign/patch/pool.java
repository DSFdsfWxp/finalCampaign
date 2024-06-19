package finalCampaign.patch;

import java.io.*;
import arc.struct.*;
import arc.files.*;
import finalCampaign.*;
import javassist.*;
import mindustry.Vars;

public class pool {
    private static ClassPool classPool = ClassPool.getDefault();
    private static patchClassLoader classLoader = new patchClassLoader(classPool.getClass().getClassLoader());
    private static boolean inited = false;
    private static ObjectMap<String, Class<?>> classMap = new ObjectMap<>();

    public static void init() throws NotFoundException {
        if (inited) return;

        Fi classDir = finalCampaign.dataDir.child("class");
        ZipFi thisModFi = finalCampaign.thisModFi;
        
        Fi mindustryClassFile = classDir.child("mindustry.jar");
        Fi javaClassFile = classDir.child("java.jar");

        if (!mindustryClassFile.exists() && Vars.android)
            thisModFi.child("class").child("mindustry.jar").copyTo(mindustryClassFile);
        if (!javaClassFile.exists() && Vars.android)
            thisModFi.child("class").child("java.jar").copyTo(javaClassFile);

        classPool.appendClassPath(finalCampaign.thisMod.file.absolutePath());
        if (Vars.android) classPool.appendClassPath(mindustryClassFile.absolutePath());
        if (Vars.android) classPool.appendClassPath(javaClassFile.absolutePath());

        inited = true;
    }

    // Class load

    protected static Class<?> loadCtClass(CtClass ctClass) throws Exception {
        String className = ctClass.getName();
        if (classMap.containsKey(className)) return classMap.get(className);

        Class<?> loadedCtClass = classLoader.loadCtClass(ctClass);
        classMap.put(className, loadedCtClass);
        return loadedCtClass;
    }

    protected static Class<?> loadClassBinary(String name, byte[] bin) {
        if (classMap.containsKey(name)) return classMap.get(name);

        Class<?> loadedClass = classLoader.loadClassBinary(name, bin);
        classMap.put(name, loadedClass);
        return loadedClass;
    }

    protected static Class<?> loadDexFile(String name, Fi file) throws Exception {
        if (classMap.containsKey(name)) return classMap.get(name);

        Class<?> loadedClass = classLoader.loadDexFile(name, file);
        classMap.put(name, loadedClass);
        return loadedClass;
    }

    // CtClass

    protected static boolean hasCtClass(String name) {
        try {
            classPool.get(name);
        } catch(NotFoundException e) {
            return false;
        }

        return true;
    }

    protected static CtClass resolveCtClass(String type, String patchClassName, String targetClassName) throws NotFoundException {
        String name = util.nameBuilder(type, util.shortHashName(patchClassName), targetClassName);
        return classPool.get(name);
    }

    protected static CtClass resolveCtClass(String name) throws NotFoundException {
        return classPool.get(name);
    }

    protected static CtClass makeCtClass(InputStream stream) throws IOException {
        return classPool.makeClass(stream);
    }

    protected static CtClass makeCtClass(String name) {
        return classPool.makeClass(name);
    }

    // Compile

    protected static void clearImportedPackages() {
        classPool.clearImportedPackages();
    }

    protected static void importPackages(String[] lst) {
        for (String packageName : lst) classPool.importPackage(packageName);
    }

    // Class resolve

    private static Class<?> resolveClass(String type, String patchClassName, String targetClassName) {
        String name = util.nameBuilder(type, util.shortHashName(patchClassName), targetClassName);
        
        if (!classMap.containsKey(name)) throw new RuntimeException("Class not found: " + name);
        return classMap.get(name);
    }

    public static Class<?> resolveModifiedTargetClass(Class<?> patchClass, Class<?> targetClass) {
        return resolveClass("modified.target", patchClass.getName(), targetClass.getName());
    }

    public static Class<?> resolveAllProxiedClass(Class<?> targetClass) {
        return resolveClass("proxied.all", null, targetClass.getName());
    }

    public static Class<?> resolveProxiedTargetClass(Class<?> patchClass, Class<?> targetClass) {
        return resolveClass("proxied.target", patchClass.getName(), targetClass.getName());
    }
}
