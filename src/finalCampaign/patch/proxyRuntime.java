package finalCampaign.patch;

import java.lang.reflect.*;
import arc.struct.*;
//import arc.util.Log;
import javassist.Modifier;

@SuppressWarnings("rawtypes")
public class proxyRuntime {

    public static Method getMethod(Object target, String name, String parameterTypeLst, String returnType) {
        Method[] methods = target.getClass().getDeclaredMethods();

        //String tmp = "";

        for (Method method : methods) {
            if (!method.getName().equals(name)) continue;
            if (!util.getTypeExpression(method.getReturnType().getName()).equals(returnType)) continue;

            Seq<String> methodParameterTypeLst = new Seq<>();
            Class[] methodParameterTypes = method.getParameterTypes();
            for (Class methodParameterType : methodParameterTypes) methodParameterTypeLst.add(util.getTypeExpression(methodParameterType.getName()));

            //tmp += util.getTypeExpression(method.getReturnType().getName()) + " " + method.getName() + "(" + String.join(",", methodParameterTypeLst) + ")" + "\n";

            //if (!method.getName().equals(name)) continue;
            //if (!util.getTypeExpression(method.getReturnType().getName()).equals(returnType)) continue;

            if (!parameterTypeLst.equals(String.join(",", methodParameterTypeLst))) continue;

            return method;
        }

        //Log.info(tmp);

        throw new RuntimeException("Method not found: " + returnType + " " + target.getClass().getName() + "." + name + "(" + parameterTypeLst + ")");
    }

    public static Object getTargetObject(Object proxy) throws IllegalAccessException, InvocationTargetException {

        Method m;

        try {
            m = proxy.getClass().getDeclaredMethod("fcPatchTargetObject_83f7f0_get");
            
        }catch(NoSuchMethodException e) {
            throw new RuntimeException("Not a proxy object: " + proxy.toString());
        }

        return m.invoke(proxy);
    }

    /** reverse: true: target -> proxy , false: proxy -> target */
    public static void syncProxyField(Object proxy, boolean reverse) throws IllegalAccessException, InvocationTargetException {

        Method fm;
        Object target;

        target = getTargetObject(proxy);
        if (target == null) throw new RuntimeException("The target object of the proxy object is not set.");

        try {
            fm = proxy.getClass().getDeclaredMethod("fcPatchTargetObject_83f7f0_syncField", Field.class, Boolean.class);
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

    public static void setProxyTarget(Object proxy, Object target) throws IllegalAccessException, InvocationTargetException {

        Method m;
        
        try {
            m = proxy.getClass().getDeclaredMethod("fcPatchTargetObject_83f7f0_set", Object.class);
            
        }catch(NoSuchMethodException e) {
            throw new RuntimeException("Not a proxy object: " + proxy.toString());
        }

        m.invoke(proxy, target);

        syncProxyField(proxy, true);
    }
}
