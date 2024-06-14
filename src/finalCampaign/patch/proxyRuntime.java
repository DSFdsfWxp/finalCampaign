package finalCampaign.patch;

import java.io.*;
import java.lang.reflect.*;
import arc.struct.*;
import javassist.*;
import javassist.Modifier;

@SuppressWarnings("rawtypes")
public class proxyRuntime {

    private static ObjectMap<String, String> targetObjectSetMethodNameMap = new ObjectMap<>();
    private static ObjectMap<String, Object> loadedAllPatchedClassMap = new ObjectMap<>();

    public static Method getMethod(Object target, String name, String parameterTypeLst, String returnType) {
        Method[] methods = target.getClass().getDeclaredMethods();

        for (Method method : methods) {
            if (!method.getName().equals(name)) continue;
            if (!method.getReturnType().getName().equals(returnType)) continue;

            Seq<String> methodParameterTypeLst = new Seq<>();
            Class[] methodParameterTypes = method.getParameterTypes();
            for (Class methodParameterType : methodParameterTypes) methodParameterTypeLst.add(methodParameterType.getName());

            if (!parameterTypeLst.equals(String.join(",", methodParameterTypeLst))) continue;

            return method;
        }

        throw new RuntimeException("Method not found: " + returnType + " " + target.getClass().getName() + "." + name + "(" + parameterTypeLst + ")");
    }

    protected static void cacheTargetObjectName(String patchClass, String name) {
        if (targetObjectSetMethodNameMap.containsKey(patchClass)) throw new RuntimeException("Making a proxy patch to the same class again is not allowed.");
        targetObjectSetMethodNameMap.put(patchClass, name);
    }

    public static <T> Object getTargetObject(Class<T> patchClass, Object proxy) throws IllegalAccessException, InvocationTargetException {
        return getTargetObject(patchClass.getName(), proxy);
    }

    protected static Object getTargetObject(String patchClassName, Object proxy) throws IllegalAccessException, InvocationTargetException {
        if (!targetObjectSetMethodNameMap.containsKey(patchClassName)) throw new RuntimeException("Can not found cached target object name for: " + patchClassName);

        Method m;

        try {
            m = proxy.getClass().getDeclaredMethod(targetObjectSetMethodNameMap.get(patchClassName) + "_get");
            
        }catch(NoSuchMethodException e) {
            throw new RuntimeException("Not a proxy object: " + proxy.toString());
        }

        return m.invoke(m);
    }

    /** reverse: true: target -> proxy , false: proxy -> target */
    public static <T> void syncProxyField(Class<T> patchClass, Object proxy, boolean reverse) throws IllegalAccessException, InvocationTargetException {
        syncProxyField(patchClass.getName(), proxy, reverse);
    }

    /** reverse: true: target -> proxy , false: proxy -> target */
    protected static void syncProxyField(String patchClassName, Object proxy, boolean reverse) throws IllegalAccessException, InvocationTargetException {
        if (!targetObjectSetMethodNameMap.containsKey(patchClassName)) throw new RuntimeException("Can not found cached target object name for: " + patchClassName);

        Method fm;
        Object target;

        target = getTargetObject(patchClassName, proxy);
        if (target == null) throw new RuntimeException("The target object of the proxy object is not set.");

        try {
            fm = proxy.getClass().getDeclaredMethod(targetObjectSetMethodNameMap.get(patchClassName) + "_syncField", Field.class, Boolean.class);
        }catch(NoSuchMethodException e) {
            throw new RuntimeException("Not a proxy object: " + proxy.toString());
        }

        Field[] fields = proxy.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isFinal(field.getModifiers()) || Modifier.isPrivate(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) continue;
            
            try {
                Field targetField = target.getClass().getDeclaredField(field.getName());
                if (targetField.getType() != field.getType()) continue;
            } catch(NoSuchFieldException e) {
                continue;
            }

            fm.invoke(proxy, field, reverse ? Boolean.TRUE : Boolean.FALSE);
        }
    }

    public static <T> void setProxyTarget(Class<T> patchClass, Object proxy, Object target) throws IllegalAccessException, InvocationTargetException {
        setProxyTarget(patchClass.getName(), proxy, target);
    }

    protected static void setProxyTarget(String patchClassName, Object proxy, Object target) throws IllegalAccessException, InvocationTargetException {
        if (!targetObjectSetMethodNameMap.containsKey(patchClassName)) throw new RuntimeException("Can not found cached target object name for: " + patchClassName);

        Method m;
        
        try {
            m = proxy.getClass().getDeclaredMethod(targetObjectSetMethodNameMap.get(patchClassName) + "_set", Object.class);
            
        }catch(NoSuchMethodException e) {
            throw new RuntimeException("Not a proxy object: " + proxy.toString());
        }

        m.invoke(proxy, target);

        syncProxyField(patchClassName, proxy, true);
    }

    public static <T> void loadAllPatchedClass(Class<T> patchClass, CtClass allPatchedClass) throws IOException, CannotCompileException {
        if (loadedAllPatchedClassMap.containsKey(patchClass.getName())) return;

        loadedAllPatchedClassMap.put(patchClass.getName(), pool.classLoader.invokeDefineClass(allPatchedClass));
    }
}
