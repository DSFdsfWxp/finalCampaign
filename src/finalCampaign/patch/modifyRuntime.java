package finalCampaign.patch;

import java.io.IOException;
import java.lang.reflect.*;
import arc.struct.*;
import javassist.*;

public class modifyRuntime {
    
    private static ObjectMap<String, String> patchClassTargetClassNameMap = new ObjectMap<>();
    private static ObjectMap<String, Object> proxyPatchClassMap = new ObjectMap<>();
    private static ObjectMap<String, CtClass> proxyPatchCtClassMap = new ObjectMap<>();

    protected static void cacheProxyPatchClass(String patchClassName, String targetClassName, Object proxyPatchClass, CtClass proxyPatchCtClass) throws CannotCompileException, IOException {
        patchClassTargetClassNameMap.put(patchClassName, targetClassName);
        proxyPatchClassMap.put(targetClassName, proxyPatchClass);
        proxyPatchCtClassMap.put(targetClassName, proxyPatchCtClass);
    }

    @SuppressWarnings("rawtypes")
    public static Class resolveProxyClass(Class patchClass) {
        String patchClassName = patchClass.getName();
        if (!patchClassTargetClassNameMap.containsKey(patchClassName)) throw new RuntimeException("Proxy patch class for modification patch class \"" + patchClassName + "\" is not found.");

        return (Class) proxyPatchClassMap.get(patchClassTargetClassNameMap.get(patchClassName));
    }

    @SuppressWarnings("rawtypes")
    public static CtClass resolveProxyCtClass(Class patchClass) {
        String patchClassName = patchClass.getName();
        if (!patchClassTargetClassNameMap.containsKey(patchClassName)) throw new RuntimeException("Proxy patch class for modification patch class \"" + patchClassName + "\" is not found.");

        return proxyPatchCtClassMap.get(patchClassTargetClassNameMap.get(patchClassName));
    }

    public static <T> void setProxyTarget(Class<T> patchClass, Object proxy, Object target) throws IllegalAccessException, InvocationTargetException {
        proxyRuntime.setProxyTarget(patchClassTargetClassNameMap.get(patchClass.getName()), proxy, target);
    }

    /** reverse: true: target -> proxy , false: proxy -> target */
    public static <T> void syncProxyField(Class<T> patchClass, Object proxy, boolean reverse) throws IllegalAccessException, InvocationTargetException {
        proxyRuntime.syncProxyField(patchClassTargetClassNameMap.get(patchClass.getName()), proxy, reverse);
    }

    public static <T> Object getProxyTarget(Class<T> patchClass, Object proxy) throws IllegalAccessException, InvocationTargetException {
        return proxyRuntime.getTargetObject(patchClassTargetClassNameMap.get(patchClass.getName()), proxy);
    }

}
