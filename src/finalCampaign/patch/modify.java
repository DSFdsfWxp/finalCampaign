package finalCampaign.patch;

import arc.struct.*;
import finalCampaign.patch.annotation.*;
import javassist.*;

@SuppressWarnings("rawtypes")
public class modify {

    protected static Class getTargetClass(Class patchClass) throws ClassNotFoundException {
        Object[] annotations = patchClass.getAnnotations();

        for (Object annotation : annotations) {
            if (annotation instanceof PatchModify modifyAnnotation) {
                return modifyAnnotation.value();
            }
        }

        throw new RuntimeException("Not a modification patch class.");
    }

    protected static Class getTargetClass(CtClass patchClass) throws ClassNotFoundException {
        Object[] annotations = patchClass.getAnnotations();

        for (Object annotation : annotations) {
            if (annotation instanceof PatchModify modifyAnnotation) {
                return modifyAnnotation.value();
            }
        }

        throw new RuntimeException("Not a modification patch class.");
    }

    protected static CtClass getTargetCtClass(Class patchClass) throws NotFoundException, ClassNotFoundException {
        return pool.resolveCtClass(getTargetClass(patchClass).getName());
    }

    protected static CtClass getTargetCtClass(CtClass patchClass) throws NotFoundException, ClassNotFoundException {
        return pool.resolveCtClass(getTargetClass(patchClass).getName());
    }

    public static Class<?> patch(Class<?> patchClass, boolean proxy) throws Exception {
        return dynamicPatch(patchClass, getTargetClass(patchClass), proxy);
    }

    public static Class<?> dynamicPatch(Class<?> patchClass, Class<?> targetClass, boolean proxy) throws Exception {
        CtClass patchedClass = patch(pool.resolveCtClass(patchClass.getName()), pool.resolveCtClass(targetClass.getName()), proxy);

        String patchClassHashName = util.shortHashName(patchClass.getName());
        if (cache.has("modified.target", patchClassHashName, targetClass.getName()))
            return cache.resolve("modified.target", patchClassHashName, targetClass.getName());
            
        return pool.loadCtClass(patchedClass);
    }

    protected static CtClass patch(CtClass patchClass, CtClass targetClass, boolean makeProxy) throws Exception {
        String patchClassHashName = util.shortHashName(patchClass.getName());
        
        if (cache.has("modified.target", patchClassHashName, targetClass.getName())) {
            CtClass result = cache.resolveCtClass("modified.target", patchClassHashName, targetClass.getName());

            if (makeProxy) {
                cache.resolveCtClass("proxied.all", null, targetClass.getName());
                cache.resolve("proxied.all", null, targetClass.getName());
            }

            return result;
        }

        targetClass.setName(util.nameBuilder("modified.target", patchClassHashName, targetClass.getName()));
        String[] importPackages = util.getPatchImport(patchClass);

        pool.clearImportedPackages();
        pool.importPackages(importPackages);

        if (!patchClass.getSuperclass().getName().equals(targetClass.getSuperclass().getName()))
            targetClass.setSuperclass(patchClass.getSuperclass());

        CtField[] pFields = patchClass.getDeclaredFields();
        for (CtField pField : pFields) {
            switch (util.getPatchOperation(pField)){
                case Add: {
                    if (util.hasField(targetClass, pField.getName()))
                        throw new RuntimeException("There is already a field called \""+pField.getName()+"\".");

                    CtField newField = new CtField(pField.getType(), pField.getName(), targetClass);
                    newField.setModifiers(pField.getModifiers());
                    targetClass.addField(newField);
                    break;
                }
                case Access: {
                    if (!util.hasField(targetClass, pField.getName()))
                        throw new RuntimeException("There is not a field called \""+pField.getName()+"\".");

                    CtField targetField = targetClass.getDeclaredField(pField.getName());

                    if (pField.getType() != targetField.getType()) throw new RuntimeException("Type conflict.");
                    if (!Modifier.isPrivate(targetField.getModifiers()))
                        throw new RuntimeException("Access patch for a non-private member is not allowed.");

                    targetField.setModifiers(Modifier.setPublic(targetField.getModifiers()));
                    break;
                }
                case Replace: {
                    if (!util.hasField(targetClass, pField.getName()))
                        throw new RuntimeException("There is not a field called \""+pField.getName()+"\".");

                    CtField targetField = targetClass.getField(pField.getName());

                    targetField.setModifiers(pField.getModifiers());
                    break;
                }
                case SuperCall: {
                    throw new RuntimeException("SuperCall is not for a field.");
                }
                case None: {
                    continue;
                }
            }
        }

        CtConstructor[] pConstructors = patchClass.getDeclaredConstructors();
        for (CtConstructor pConstructor : pConstructors) {
            switch (util.getPatchOperation(pConstructor)){
                case Add: {
                    if (util.hasBehavior(targetClass, targetClass.getSimpleName(), pConstructor.getParameterTypes()))
                        throw new RuntimeException("There is already a constructor called \""+pConstructor.getName()+"\".");

                    CtConstructor newConstructor = new CtConstructor(pConstructor.getParameterTypes(), targetClass);
                    newConstructor.setModifiers(pConstructor.getModifiers());
                    newConstructor.setBody(pConstructor, null);
                    targetClass.addConstructor(newConstructor);
                    break;
                }
                case Access: {
                    if (!util.hasBehavior(targetClass, targetClass.getSimpleName(), pConstructor.getParameterTypes()))
                        throw new RuntimeException("There is not a constructor called \""+pConstructor.getName()+"\".");

                    CtConstructor targetConstructor = targetClass.getDeclaredConstructor(pConstructor.getParameterTypes());

                    if (!Modifier.isPrivate(targetConstructor.getModifiers()))
                        throw new RuntimeException("Access patch for a not private member is not allowed.");

                    targetConstructor.setModifiers(Modifier.setPublic(targetConstructor.getModifiers()));
                    break;
                }
                case Replace: {
                    if (!util.hasBehavior(targetClass, targetClass.getSimpleName(), pConstructor.getParameterTypes()))
                        throw new RuntimeException("There is not a constructor called \""+pConstructor.getName()+"\".");

                    CtConstructor targetConstructor = targetClass.getDeclaredConstructor(pConstructor.getParameterTypes());

                    targetConstructor.setModifiers(pConstructor.getModifiers());
                    targetConstructor.setBody(pConstructor, null);
                    break;
                }
                case SuperCall: {
                    throw new RuntimeException("SuperCall is not for a constructor.");
                }
                case None: {
                    continue;
                }
            }
        }

        CtMethod[] pMethods = patchClass.getDeclaredMethods();
        for (CtMethod pMethod : pMethods) {
            switch (util.getPatchOperation(pMethod)){
                case Replace: {
                    if (!util.hasBehavior(targetClass, pMethod.getName(), pMethod.getParameterTypes()))
                        throw new RuntimeException("There is not a method called \""+pMethod.getName()+"\".");

                    CtMethod targetMethod = targetClass.getDeclaredMethod(pMethod.getName(), pMethod.getParameterTypes());

                    if (targetMethod.getReturnType() != pMethod.getReturnType()) throw new RuntimeException("Return type conflict.");
                    if (targetMethod.getModifiers() != pMethod.getModifiers()) throw new RuntimeException("Modifiers conflict.");

                    targetClass.removeMethod(targetMethod);
                }
                case Add: {
                    if (util.hasBehavior(targetClass, pMethod.getName(), pMethod.getParameterTypes()))
                        throw new RuntimeException("There is already a method called \""+pMethod.getName()+"\".");

                    CtMethod newMethod = new CtMethod(pMethod, targetClass, null);
                    newMethod.setModifiers(pMethod.getModifiers());
                    targetClass.addMethod(newMethod);
                    break;
                }
                case Access: {
                    if (!util.hasBehavior(targetClass, pMethod.getName(), pMethod.getParameterTypes()))
                        throw new RuntimeException("There is not a method called \""+pMethod.getName()+"\".");

                    CtMethod targetMethod = targetClass.getDeclaredMethod(pMethod.getName(), pMethod.getParameterTypes());

                    if (!Modifier.isPrivate(targetMethod.getModifiers())) 
                        throw new RuntimeException("Access patch for a not private member is not allowed.");
                    if (targetMethod.getReturnType() != pMethod.getReturnType()) throw new RuntimeException("Return type conflict.");

                    targetMethod.setModifiers(Modifier.setPublic(targetMethod.getModifiers()));
                    break;
                }
                case SuperCall: {
                    CtMethod newMethod = new CtMethod(pMethod.getReturnType(), pMethod.getName(), pMethod.getParameterTypes(), targetClass);
                    newMethod.setModifiers(pMethod.getModifiers());

                    String prefix = pMethod.getReturnType() == CtClass.voidType ? "" : "return ";
                    newMethod.setBody("{ " + prefix + "super." + newMethod.getName() + "($$); }");

                    targetClass.addMethod(newMethod);
                    break;
                }
                case None: {
                    continue;
                }
            }
        }

        CtClass[] pClasses = patchClass.getDeclaredClasses();
        for (CtClass pClass : pClasses) {
            CtClass currentClass = pClass;
            Seq<Object> annotations = util.getPatchAnnotations(new Seq<Object>(pClass.getAnnotations()));
            Boolean modified = false;

            for (Object annotation : annotations) {
                if (annotation instanceof PatchModify) {
                    if (modified) throw new RuntimeException("Repatch a sub class is not allowed.");
                    currentClass = patch(pClass, getTargetCtClass(pClass), false);
                    modified = true;
                }

                if (annotation instanceof PatchProxy) throw new RuntimeException("Can not make a proxy patch to a sub class.");
                if (annotation instanceof PatchAccess) throw new RuntimeException("Operation is not allowed to a sub class.");

                if (annotation instanceof PatchAdd) {
                    if (util.hasNestClass(targetClass, currentClass.getSimpleName()))
                        throw new RuntimeException("There is already a nest class called \"" + util.getRealClassName(currentClass.getSimpleName()) + "\"");
                    
                    util.copyClass(currentClass, targetClass.makeNestedClass(util.getRealClassName(currentClass.getSimpleName()), Modifier.isStatic(currentClass.getModifiers())));
                } else {
                    CtClass targetNestClass = util.getNestClass(targetClass, util.getRealClassName(currentClass.getSimpleName()));
                    util.clearClass(targetNestClass);
                    util.copyClass(currentClass, targetNestClass);
                }
            }
        }

        if (makeProxy) {
            CtClass proxyPatchClass = proxy.patchAll(getTargetCtClass(patchClass));
            pool.loadCtClass(proxyPatchClass);
        }

        pool.clearImportedPackages();

        return targetClass;
    }
}
