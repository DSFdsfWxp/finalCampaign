package finalCampaign.patch;

import java.io.*;
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
                if (targetClass.equals(Object.class)) throw new RuntimeException("Missing target class in a proxy patch class.");

                return targetClass;
            }
        }

        throw new RuntimeException("Not a proxy patch class.");
    }

    protected static CtClass getTargetCtClass(Class patchClass) throws ClassNotFoundException, NotFoundException {
        return pool.classPool.get(getTargetClass(patchClass).getName());
    }

    protected static CtClass getTargetCtClass(CtClass patchClass) throws NotFoundException {
        return patchClass.getSuperclass();
    }

    public static <T> CtClass patch(Class<T> patchClass) throws NotFoundException, CannotCompileException, ClassNotFoundException, IOException {
        CtClass patchCtClass = pool.classPool.get(patchClass.getName());
        CtClass targetClass = getTargetCtClass(patchCtClass);
        String random = util.randomName();
        CtClass allPatchedClass = patchAll(patchCtClass, targetClass, random);

        proxyRuntime.loadAllPatchedClass(patchClass, allPatchedClass);

        CtClass patchedClass = pool.classPool.makeClass("finalCampaign.patch.proxied.patched." + random + "." + targetClass.getName());
        patchedClass.setSuperclass(allPatchedClass);
        patchedClass.setInterfaces(patchCtClass.getInterfaces());

        Seq<String> importPackages = new Seq<>(util.getPatchImport(patchCtClass));
        pool.classPool.clearImportedPackages();
        for (String importPackage : importPackages) pool.classPool.importPackage(importPackage);

        CtField[] pFields = patchCtClass.getDeclaredFields();
        for (CtField pField : pFields) {
            switch (util.getPatchOperation(pField)) {
                case Add: 
                    if (util.hasField(targetClass, pField.getName())) throw new RuntimeException("There is already a field called \"" + pField.getName() + "\".");
                case Replace: {
                    CtField newField = new CtField(pField, patchedClass);
                    newField.setModifiers(pField.getModifiers());
                    patchedClass.addField(newField);
                    break;
                }
                case Access: 
                    throw new RuntimeException("Operation access in proxy patch is not allowed.");
                case None:
                    continue;
            }
        }

        CtConstructor[] pConstructors = patchCtClass.getDeclaredConstructors();
        for (CtConstructor pConstructor : pConstructors) {
            switch (util.getPatchOperation(pConstructor)) {
                case Add:
                    if (util.hasBehavior(targetClass, pConstructor.getName(), pConstructor.getParameterTypes())) throw new RuntimeException("There is already a constructor called \"" + pConstructor.getName() + "\".");
                case Replace: {
                    CtConstructor newConstructor = new CtConstructor(pConstructor, patchedClass, null);
                    newConstructor.setModifiers(pConstructor.getModifiers());
                    patchedClass.addConstructor(newConstructor);
                    break;
                }
                case Access:
                    throw new RuntimeException("Operation access in proxy patch is not allowed.");
                case None:
                    continue;
            }
        }

        CtMethod[] pMethods = patchCtClass.getDeclaredMethods();
        for (CtMethod pMethod : pMethods) {
            switch (util.getPatchOperation(pMethod)) {
                case Add:
                    if (util.hasBehavior(targetClass, pMethod.getName(), pMethod.getParameterTypes())) throw new RuntimeException("There is already a method called \"" + pMethod.getName() + "\".");
                case Replace: {
                    CtMethod newMethod = new CtMethod(pMethod, patchedClass, null);
                    newMethod.setModifiers(pMethod.getModifiers());
                    patchedClass.addMethod(newMethod);
                    break;
                }
                case Access:
                    throw new RuntimeException("Operation access in proxy patch is not allowed.");
                case None:
                    continue;
            }
        }

        CtClass[] pClasses = patchCtClass.getDeclaredClasses();
        for (CtClass pClass : pClasses) {
            CtClass currentClass = pClass;
            Seq<Object> annotations = util.getPatchAnnotations(new Seq<>(pClass.getAnnotations()));
            boolean modified = false;

            for (Object annotation : annotations) {
                if (annotation instanceof PatchModify) {
                    if (modified) throw new RuntimeException("Repatch a sub class is not allowed.");
                    currentClass = modify.patch(pClass, false);
                    modified = true;
                }

                if (annotation instanceof PatchProxy) throw new RuntimeException("Can not make a proxy patch to a sub class.");
                if (annotation instanceof PatchAccess) throw new RuntimeException("Operation is not allowed to a sub class.");

                if (annotation instanceof PatchAdd) {
                    if (util.hasNestClass(patchedClass, currentClass.getSimpleName())) throw new RuntimeException("There is already a nest class called \"" + util.getRealClassName(currentClass.getSimpleName()) + "\"");
                    
                    util.copyClass(currentClass, patchedClass.makeNestedClass(util.getRealClassName(currentClass.getSimpleName()), Modifier.isStatic(currentClass.getModifiers())));
                } else {
                    CtClass originNestClass = util.getNestClass(patchedClass, util.getRealClassName(currentClass.getSimpleName()));
                    util.clearClass(originNestClass);
                    util.copyClass(currentClass, originNestClass);
                }
            }
        }

        pool.classPool.clearImportedPackages();

        return patchedClass;
    }

    protected static CtClass patchAll(CtClass patchClass, CtClass targetClass, String random) throws NotFoundException, CannotCompileException {
        targetClass = pool.classPool.get(targetClass.getName());
        CtClass patchedClass = pool.classPool.makeClass("finalCampaign.patch.proxied.all." + random + '.' + targetClass.getName());
        String targetObjectName = "fcPatchTargetObject_" + util.randomName();

        final String[] importPackages = {"java.lang.reflect", "finalCampaign.patch"};
        pool.classPool.clearImportedPackages();
        for (String importPackage : importPackages) pool.classPool.importPackage(importPackage);

        patchedClass.setSuperclass(targetClass);

        CtField[] targetFields = targetClass.getDeclaredFields();
        for (CtField targetField : targetFields) {
            if (Modifier.isPrivate(targetField.getModifiers()) || Modifier.isFinal(targetField.getModifiers()) || Modifier.isStatic(targetField.getModifiers())) continue;

            CtField newField = new CtField(targetField, patchedClass);
            patchedClass.addField(newField);
        }

        CtField targetObjectField = new CtField(pool.classPool.get("java.lang.Object"), targetObjectName, patchedClass);
        targetObjectField.setModifiers(Modifier.PRIVATE);
        patchedClass.addField(targetObjectField);

        CtMethod targetObjectSetMethod = new CtMethod(CtClass.voidType, targetObjectName + "_set", new CtClass[]{pool.classPool.get("java.lang.Object")}, patchedClass);
        targetObjectSetMethod.setModifiers(Modifier.PUBLIC);
        targetObjectSetMethod.setBody("{ if($0." + targetObjectName + "!=null) $0." + targetObjectName + "=$1; }");
        patchedClass.addMethod(targetObjectSetMethod);
        
        CtMethod selfFieldSyncMethod = new CtMethod(CtClass.voidType, targetObjectName + "_syncField", new CtClass[]{pool.classPool.get("java.lang.reflect.Field"), pool.classPool.get("java.lang.Boolean")}, patchedClass);
        selfFieldSyncMethod.setModifiers(Modifier.PUBLIC);
        selfFieldSyncMethod.setBody("{ if ($2.booleanValue()) { $1.set($0,$1.get($0." + targetObjectName + ")); } else { $1.set($0." + targetObjectName + ",$1.get($0)); } }");
        patchedClass.addMethod(selfFieldSyncMethod);

        CtMethod targetObjectGetMethod = new CtMethod(pool.classPool.get("java.lang.Object"), targetObjectName + "_get", new CtClass[]{}, patchedClass);
        targetObjectGetMethod.setModifiers(Modifier.PUBLIC);
        targetObjectGetMethod.setBody("{ return $0." + targetObjectName + "; }");
        patchedClass.addMethod(targetObjectGetMethod);

        CtConstructor[] targetConstructors = targetClass.getDeclaredConstructors();
        for (CtConstructor targetConstructor : targetConstructors) {
            if (Modifier.isPrivate(targetConstructor.getModifiers()) || Modifier.isStatic(targetConstructor.getModifiers())) continue;

            CtConstructor newConstructor = new CtConstructor(targetConstructor.getParameterTypes(), patchedClass);
            newConstructor.setModifiers(targetConstructor.getModifiers());
            newConstructor.setBody("{ $0." + targetObjectName + "=null; $proceed($$); }");
            newConstructor.addCatch("{ throw new RuntimeException($e); }", pool.classPool.get("java.lang.Exception"));

            patchedClass.addConstructor(newConstructor);
        }

        CtMethod[] targetMethods = targetClass.getDeclaredMethods();
        for (CtMethod targetMethod : targetMethods) {
            if (Modifier.isPrivate(targetMethod.getModifiers()) || Modifier.isStatic(targetMethod.getModifiers())) continue;

            CtMethod newMethod = new CtMethod(targetMethod.getReturnType(), targetMethod.getName(), targetMethod.getParameterTypes(), patchedClass);
            newMethod.setModifiers(targetMethod.getModifiers());

            CtClass[] parameters = targetMethod.getParameterTypes();
            Seq<String> parameterLst = new Seq<>();
            for (CtClass parameter : parameters) parameterLst.add(parameter.getName());
            
            Seq<String> src = new Seq<>();
            src.add("Method m=proxyRuntime.getMethod($0." + targetObjectName + ",\"" + targetMethod.getName() + "\",\"" + String.join(",", parameterLst) + "\",\"" + targetMethod.getReturnType().getName() + "\");");
            src.add(((targetMethod.getReturnType() == CtClass.voidType) ? "" : "return ") + "m.invoke($0,$$);");

            newMethod.setBody("{ " + String.join(" ", src) + " }");
            newMethod.addCatch("{ throw new RuntimeException($e); }", pool.classPool.get("java.lang.Exception"));

            patchedClass.addMethod(newMethod);
        }

        proxyRuntime.cacheTargetObjectName(patchClass.getName(), targetObjectName);

        pool.classPool.clearImportedPackages();

        return patchedClass;
    }
}
