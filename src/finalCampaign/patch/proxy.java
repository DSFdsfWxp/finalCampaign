package finalCampaign.patch;

import java.io.*;
import java.security.*;

import arc.struct.*;
import javassist.*;
import finalCampaign.patch.annotation.*;

@SuppressWarnings("rawtypes")
public class proxy {

    protected static Class getTargetClass(Class patchClass) throws ClassNotFoundException {
        Object[] annotations = patchClass.getAnnotations();

        for (Object annotation : annotations) {
            if (annotation instanceof PatchProxy) {
                Class targetClass = patchClass.getSuperclass();
                if (targetClass.equals(Object.class))
                    throw new RuntimeException("Missing target class in a proxy patch class.");

                return targetClass;
            }
        }

        throw new RuntimeException("Not a proxy patch class.");
    }

    protected static CtClass getTargetCtClass(Class patchClass) throws ClassNotFoundException, NotFoundException {
        return pool.resolveCtClass(getTargetClass(patchClass).getName());
    }

    protected static CtClass getTargetCtClass(CtClass patchClass) throws NotFoundException {
        return patchClass.getSuperclass();
    }

    public static Class<?> patch(Class<?> patchClass) throws Exception {
        Class<?> targetClass = getTargetClass(patchClass);
        return dynamicPatch(patchClass, targetClass);
    }

    public static Class<?> dynamicPatch(Class<?> patchClass, Class<?> targetClass) throws Exception {
        CtClass patchedClass = patch(pool.resolveCtClass(patchClass.getName()), getTargetCtClass(patchClass));

        String patchClassHashName = util.shortHashName(patchClass.getName());
        if (cache.has("proxied.target", patchClassHashName, targetClass.getName()))
            return cache.resolve("proxied.target", patchClassHashName, targetClass.getName());

        return pool.loadCtClass(patchedClass);
    }

    protected static <T> CtClass patch(CtClass patchClass, CtClass targetClass) throws Exception {
        String patchClassHashName = util.shortHashName(patchClass.getName());

        CtClass allPatchedClass = patchAll(targetClass);
        if (cache.has("proxied.all", null, targetClass.getName())) {
            cache.resolve("proxied.all", null, targetClass.getName());
        } else {
            pool.loadCtClass(allPatchedClass);
        }

        if (cache.has("proxied.target", patchClassHashName, targetClass.getName())) {
            return cache.resolveCtClass("proxied.target", patchClassHashName, targetClass.getName());
        }

        CtClass patchedClass = pool.makeCtClass(util.nameBuilder("proxied.target", patchClassHashName, targetClass.getName()));
        patchedClass.setSuperclass(allPatchedClass);
        patchedClass.setInterfaces(patchClass.getInterfaces());

        pool.importPackages(util.getPatchImport(patchClass));

        CtField[] pFields = patchClass.getDeclaredFields();
        for (CtField pField : pFields) {
            switch (util.getPatchOperation(pField)) {
                case Add: 
                    if (util.hasField(targetClass, pField.getName()))
                        throw new RuntimeException("There is already a field called \"" + pField.getName() + "\".");
                case Replace: {
                    CtField newField = new CtField(pField, patchedClass);
                    newField.setModifiers(pField.getModifiers());
                    patchedClass.addField(newField);
                    break;
                }
                case SuperCall:
                    throw new RuntimeException("SuperCall is not for a field.");
                case Access: 
                    throw new RuntimeException("Operation access in proxy patch is not allowed.");
                case None:
                    continue;
            }
        }

        CtConstructor[] pConstructors = patchClass.getDeclaredConstructors();
        for (CtConstructor pConstructor : pConstructors) {
            switch (util.getPatchOperation(pConstructor)) {
                case Add:
                    if (util.hasBehavior(targetClass, pConstructor.getName(), pConstructor.getParameterTypes()))
                        throw new RuntimeException("There is already a constructor called \"" + pConstructor.getName() + "\".");
                case Replace: {
                    CtConstructor newConstructor = new CtConstructor(pConstructor, patchedClass, null);
                    newConstructor.setModifiers(pConstructor.getModifiers());
                    patchedClass.addConstructor(newConstructor);
                    break;
                }
                case Access:
                    throw new RuntimeException("Operation access in proxy patch is not allowed.");
                case SuperCall:
                    throw new RuntimeException("SuperCall is not for a field.");
                case None:
                    continue;
            }
        }

        CtMethod[] pMethods = patchClass.getDeclaredMethods();
        for (CtMethod pMethod : pMethods) {
            switch (util.getPatchOperation(pMethod)) {
                case Add:
                    if (util.hasBehavior(targetClass, pMethod.getName(), pMethod.getParameterTypes()))
                        throw new RuntimeException("There is already a method called \"" + pMethod.getName() + "\".");
                case Replace: {
                    CtMethod newMethod = new CtMethod(pMethod, patchedClass, null);
                    newMethod.setModifiers(pMethod.getModifiers());
                    patchedClass.addMethod(newMethod);
                    break;
                }
                case SuperCall: {
                    CtMethod newMethod = new CtMethod(pMethod.getReturnType(), pMethod.getName(), pMethod.getParameterTypes(), patchedClass);
                    newMethod.setModifiers(pMethod.getModifiers());

                    String prefix = pMethod.getReturnType() == CtClass.voidType ? "" : "return ";
                    newMethod.setBody("{ " + prefix + "super." + newMethod.getName() + "($$); }");

                    patchedClass.addMethod(newMethod);
                    break;
                }
                case Access:
                    throw new RuntimeException("Operation access in proxy patch is not allowed.");
                case None:
                    continue;
            }
        }

        CtClass[] pClasses = patchClass.getDeclaredClasses();
        for (CtClass pClass : pClasses) {
            CtClass currentClass = pClass;
            Seq<Object> annotations = util.getPatchAnnotations(new Seq<>(pClass.getAnnotations()));
            boolean modified = false;

            for (Object annotation : annotations) {
                if (annotation instanceof PatchModify) {
                    if (modified) throw new RuntimeException("Repatch a sub class is not allowed.");
                    currentClass = modify.patch(pClass, modify.getTargetCtClass(pClass), false);
                    modified = true;
                }

                if (annotation instanceof PatchProxy) throw new RuntimeException("Can not make a proxy patch to a sub class.");
                if (annotation instanceof PatchAccess) throw new RuntimeException("Operation is not allowed to a sub class.");

                if (annotation instanceof PatchAdd) {
                    if (util.hasNestClass(patchedClass, currentClass.getSimpleName()))
                        throw new RuntimeException("There is already a nest class called \"" + util.getRealClassName(currentClass.getSimpleName()) + "\"");
                    
                    util.copyClass(currentClass, patchedClass.makeNestedClass(util.getRealClassName(currentClass.getSimpleName()), Modifier.isStatic(currentClass.getModifiers())));
                } else {
                    CtClass originNestClass = util.getNestClass(patchedClass, util.getRealClassName(currentClass.getSimpleName()));
                    util.clearClass(originNestClass);
                    util.copyClass(currentClass, originNestClass);
                }
            }
        }

        pool.clearImportedPackages();

        return patchedClass;
    }

    protected static CtClass patchAll(CtClass targetClass) throws NotFoundException, CannotCompileException, IOException, NoSuchAlgorithmException {
        if (cache.has("proxied.all", null, targetClass.getName()))
            return cache.resolveCtClass("proxied.all", null, targetClass.getName());

        CtClass patchedClass = pool.makeCtClass(util.nameBuilder("proxied.all", null, targetClass.getName()));
        String targetObjectName = "fcPatchTargetObject_83f7f0";

        final String[] importPackages = {"java.lang.reflect", "finalCampaign.patch"};
        pool.clearImportedPackages();
        pool.importPackages(importPackages);

        patchedClass.setSuperclass(targetClass);

        CtField[] targetFields = util.getAllDeclaredFields(targetClass);
        for (CtField targetField : targetFields) {
            if (Modifier.isPrivate(targetField.getModifiers()) ||
                Modifier.isFinal(targetField.getModifiers()) ||
                Modifier.isStatic(targetField.getModifiers())) continue;

            CtField newField = new CtField(targetField, patchedClass);
            patchedClass.addField(newField);
        }

        CtField targetObjectField = new CtField(pool.resolveCtClass("java.lang.Object"), targetObjectName, patchedClass);
        targetObjectField.setModifiers(Modifier.PRIVATE);
        patchedClass.addField(targetObjectField);

        CtMethod targetObjectSetMethod = new CtMethod(CtClass.voidType, targetObjectName + "_set", new CtClass[]{pool.resolveCtClass("java.lang.Object")}, patchedClass);
        targetObjectSetMethod.setModifiers(Modifier.PUBLIC);
        targetObjectSetMethod.setBody("{ if(" + targetObjectName + "==null) " + targetObjectName + "=$1; }");
        patchedClass.addMethod(targetObjectSetMethod);
        
        CtMethod selfFieldSyncMethod = new CtMethod(CtClass.voidType, targetObjectName + "_syncField", new CtClass[]{pool.resolveCtClass("java.lang.reflect.Field"), pool.resolveCtClass("java.lang.Boolean")}, patchedClass);
        selfFieldSyncMethod.setModifiers(Modifier.PUBLIC);
        selfFieldSyncMethod.setBody("{ if ($2.booleanValue()) { $1.set($0,$1.get(" + targetObjectName + ")); } else { $1.set(" + targetObjectName + ",$1.get($0)); } }");
        patchedClass.addMethod(selfFieldSyncMethod);

        CtMethod targetObjectGetMethod = new CtMethod(pool.resolveCtClass("java.lang.Object"), targetObjectName + "_get", new CtClass[]{}, patchedClass);
        targetObjectGetMethod.setModifiers(Modifier.PUBLIC);
        targetObjectGetMethod.setBody("{ return " + targetObjectName + "; }");
        patchedClass.addMethod(targetObjectGetMethod);

        CtConstructor[] targetConstructors = targetClass.getDeclaredConstructors();
        for (CtConstructor targetConstructor : targetConstructors) {
            if (Modifier.isPrivate(targetConstructor.getModifiers()) || Modifier.isStatic(targetConstructor.getModifiers())) continue;

            CtConstructor newConstructor = new CtConstructor(targetConstructor.getParameterTypes(), patchedClass);
            newConstructor.setModifiers(targetConstructor.getModifiers());
            newConstructor.setBody("{ super($$); " + targetObjectName + "=null; }");
            newConstructor.addCatch("{ throw new RuntimeException($e); }", pool.resolveCtClass("java.lang.Exception"));

            patchedClass.addConstructor(newConstructor);
        }

        CtMethod[] targetMethods = util.getAllDeclaredMethods(targetClass);
        for (CtMethod targetMethod : targetMethods) {
            if (Modifier.isPrivate(targetMethod.getModifiers()) || Modifier.isStatic(targetMethod.getModifiers())) continue;

            CtMethod newMethod = new CtMethod(targetMethod.getReturnType(), targetMethod.getName(), targetMethod.getParameterTypes(), patchedClass);
            newMethod.setModifiers(targetMethod.getModifiers());

            CtClass[] parameters = targetMethod.getParameterTypes();
            Seq<String> parameterLst = new Seq<>();
            for (CtClass parameter : parameters) parameterLst.add(parameter.getName());
            String parameterLstStr = String.join(",", parameterLst);

            String cacheHash = util.hash(parameterLstStr + ";" + targetMethod.getReturnType());
            String cacheName = "fcPatchMethodCache_83f7f0_" + targetMethod.getName() + "_" + cacheHash;
            CtField cacheField = new CtField(pool.resolveCtClass("java.lang.reflect.Method"), cacheName, patchedClass);
            cacheField.setModifiers(Modifier.PRIVATE);
            patchedClass.addField(cacheField);
            
            Seq<String> src = new Seq<>();
            src.add("if (" + cacheName + "==null) { " + cacheName + "=proxyRuntime.getMethod(" + targetObjectName + ",\"" + targetMethod.getName() + "\",\"" + parameterLstStr + "\",\"" + targetMethod.getReturnType().getName() + "\"); }");
            src.add("Object o=" + cacheName + ".invoke(" + targetObjectName + ",$args);");
            if (targetMethod.getReturnType() != CtClass.voidType) src.add("return (" + targetMethod.getReturnType().getName() + ")o;");

            newMethod.setBody("{ " + String.join(" ", src) + " }");
            newMethod.addCatch("{ throw new RuntimeException($e); }", pool.resolveCtClass("java.lang.Exception"));

            patchedClass.addMethod(newMethod);
        }

        pool.clearImportedPackages();

        return patchedClass;
    }
}
