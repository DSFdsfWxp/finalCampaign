package finalCampaign.patch;

import java.lang.reflect.*;
import arc.struct.*;

public class modifyRuntime {
    
    private static ObjectMap<String, String> patchClassTargetClassNameMap = new ObjectMap<>();
    private static ObjectMap<String, Object> proxyPatchClassMap = new ObjectMap<>();

    protected static void cacheProxyPatchClass(String patchClassName, String targetClassName, Object proxyPatchClass) {
        patchClassTargetClassNameMap.put(patchClassName, targetClassName);
        proxyPatchClassMap.put(targetClassName, proxyPatchClass);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> resolveProxyClass(Object patchClass, Class<T> targetClass) {
        String patchClassName = patchClass.getClass().getName();
        if (!patchClassTargetClassNameMap.containsKey(patchClassName)) throw new RuntimeException("Proxy patch class for modification patch class \"" + patchClassName + "\" is not found.");

        return (Class<T>) proxyPatchClassMap.get(patchClassTargetClassNameMap.get(patchClassName));
    }

    public static <T> void setProxyTarget(Class<T> patchClass, Object proxy, Object target) throws IllegalAccessException, InvocationTargetException {
        proxyRuntime.setProxyTarget(patchClassTargetClassNameMap.get(patchClass.getName()), proxy, target);
    }

    public static <T> void syncProxyField(Class<T> patchClass, Object proxy, boolean reverse) throws IllegalAccessException, InvocationTargetException {
        proxyRuntime.syncProxyField(patchClassTargetClassNameMap.get(patchClass.getName()), proxy, reverse);
    }


}
