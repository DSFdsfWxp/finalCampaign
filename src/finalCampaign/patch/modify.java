package finalCampaign.patch;

import java.io.*;
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
        return pool.classPool.get(getTargetClass(patchClass).getName());
    }

    protected static CtClass getTargetCtClass(CtClass patchClass) throws NotFoundException, ClassNotFoundException {
        return pool.classPool.get(getTargetClass(patchClass).getName());
    }

    public static <T> CtClass patch(Class<T> patchClass) throws NotFoundException, ClassNotFoundException, CannotCompileException, IOException {
        CtClass patchCtClass = pool.classPool.get(patchClass.getName());
        return patch(patchCtClass, true);
    }

    protected static CtClass patch(CtClass patchClass, boolean makeProxy) throws NotFoundException, ClassNotFoundException, CannotCompileException, IOException {
        CtClass originClass = getTargetCtClass(patchClass);
        String random = util.randomName();
        originClass.setName("finalCampaign.patch.modified.target." + random + "." + originClass.getName());
        CtClass targetClass = getTargetCtClass(patchClass);
        String[] importPackages = util.getPatchImport(patchClass);

        pool.classPool.clearImportedPackages();
        for (String importPackage : importPackages) pool.classPool.importPackage(importPackage);

        CtField[] pFields = patchClass.getDeclaredFields();
        for (CtField pField : pFields) {
            switch (util.getPatchOperation(pField)){
                case Add: {
                    if (util.hasField(originClass, pField.getName())) throw new RuntimeException("There is already a field called \""+pField.getName()+"\".");

                    CtField newField = new CtField(pField.getType(), pField.getName(), originClass);
                    newField.setModifiers(pField.getModifiers());
                    originClass.addField(newField);
                    break;
                }
                case Access: {
                    if (!util.hasField(originClass, pField.getName())) throw new RuntimeException("There is not a field called \""+pField.getName()+"\".");

                    CtField originField = originClass.getDeclaredField(pField.getName());

                    if (pField.getType() != originField.getType()) throw new RuntimeException("Type conflict.");
                    if (!Modifier.isPrivate(originField.getModifiers())) throw new RuntimeException("Access patch for a non-private member is not allowed.");

                    originField.setModifiers(Modifier.setPublic(originField.getModifiers()));
                    break;
                }
                case Replace: {
                    if (!util.hasField(originClass, pField.getName())) throw new RuntimeException("There is not a field called \""+pField.getName()+"\".");

                    CtField originField = originClass.getField(pField.getName());

                    originField.setModifiers(pField.getModifiers());
                    break;
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
                    if (util.hasBehavior(originClass, originClass.getSimpleName(), pConstructor.getParameterTypes())) throw new RuntimeException("There is already a constructor called \""+pConstructor.getName()+"\".");

                    CtConstructor newConstructor = new CtConstructor(pConstructor.getParameterTypes(), originClass);
                    newConstructor.setModifiers(pConstructor.getModifiers());
                    newConstructor.setBody(pConstructor, null);
                    originClass.addConstructor(newConstructor);
                    break;
                }
                case Access: {
                    if (!util.hasBehavior(originClass, originClass.getSimpleName(), pConstructor.getParameterTypes())) throw new RuntimeException("There is not a constructor called \""+pConstructor.getName()+"\".");

                    CtConstructor originConstructor = originClass.getDeclaredConstructor(pConstructor.getParameterTypes());

                    if (!Modifier.isPrivate(originConstructor.getModifiers())) throw new RuntimeException("Access patch for a not private member is not allowed.");

                    originConstructor.setModifiers(Modifier.setPublic(originConstructor.getModifiers()));
                    break;
                }
                case Replace: {
                    if (!util.hasBehavior(originClass, originClass.getSimpleName(), pConstructor.getParameterTypes())) throw new RuntimeException("There is not a constructor called \""+pConstructor.getName()+"\".");

                    CtConstructor originConstructor = originClass.getDeclaredConstructor(pConstructor.getParameterTypes());

                    originConstructor.setModifiers(pConstructor.getModifiers());
                    originConstructor.setBody(pConstructor, null);
                    break;
                }
                case None: {
                    continue;
                }
            }
        }

        CtMethod[] pMethods = patchClass.getDeclaredMethods();
        for (CtMethod pMethod : pMethods) {
            switch (util.getPatchOperation(pMethod)){
                case Add: {
                    if (util.hasBehavior(originClass, pMethod.getName(), pMethod.getParameterTypes())) throw new RuntimeException("There is already a method called \""+pMethod.getName()+"\".");

                    CtMethod newMethod = new CtMethod(pMethod.getReturnType(), pMethod.getName(), pMethod.getParameterTypes(), originClass);
                    newMethod.setModifiers(pMethod.getModifiers());
                    newMethod.setBody(pMethod, null);
                    originClass.addMethod(newMethod);
                    break;
                }
                case Access: {
                    if (!util.hasBehavior(originClass, pMethod.getName(), pMethod.getParameterTypes())) throw new RuntimeException("There is not a method called \""+pMethod.getName()+"\".");

                    CtMethod originMethod = originClass.getDeclaredMethod(pMethod.getName(), pMethod.getParameterTypes());

                    if (!Modifier.isPrivate(originMethod.getModifiers())) throw new RuntimeException("Access patch for a not private member is not allowed.");
                    if (originMethod.getReturnType() != pMethod.getReturnType()) throw new RuntimeException("Return type conflict.");

                    originMethod.setModifiers(Modifier.setPublic(originMethod.getModifiers()));
                    break;
                }
                case Replace: {
                    if (!util.hasBehavior(originClass, pMethod.getName(), pMethod.getParameterTypes())) throw new RuntimeException("There is not a method called \""+pMethod.getName()+"\".");

                    CtMethod originMethod = originClass.getDeclaredMethod(pMethod.getName(), pMethod.getParameterTypes());

                    if (originMethod.getReturnType() != pMethod.getReturnType()) throw new RuntimeException("Return type conflict.");
                    if (originMethod.getModifiers() != pMethod.getModifiers()) throw new RuntimeException("Modifiers conflict.");

                    originMethod.setModifiers(pMethod.getModifiers());
                    originMethod.setBody(pMethod, null);
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
                    currentClass = patch(pClass, false);
                    modified = true;
                }

                if (annotation instanceof PatchProxy) throw new RuntimeException("Can not make a proxy patch to a sub class.");
                if (annotation instanceof PatchAccess) throw new RuntimeException("Operation is not allowed to a sub class.");

                if (annotation instanceof PatchAdd) {
                    if (util.hasNestClass(originClass, currentClass.getSimpleName())) throw new RuntimeException("There is already a nest class called \"" + util.getRealClassName(currentClass.getSimpleName()) + "\"");
                    
                    util.copyClass(currentClass, originClass.makeNestedClass(util.getRealClassName(currentClass.getSimpleName()), Modifier.isStatic(currentClass.getModifiers())));
                } else {
                    CtClass originNestClass = util.getNestClass(originClass, util.getRealClassName(currentClass.getSimpleName()));
                    util.clearClass(originNestClass);
                    util.copyClass(currentClass, originNestClass);
                }
            }
        }

        if (makeProxy) {
            Object loadedOriginClass = pool.classLoader.invokeDefineClass(originClass);
            CtClass proxyPatchClass = proxy.patchAll(originClass, targetClass, random);
            
            proxyPatchClass.setName("finalCampaign.patch.modified.proxied." + random + "." + targetClass.getName());
            Object loadedProxyPatchClass = pool.classLoader.invokeDefineClass(proxyPatchClass);

            modifyRuntime.cacheProxyPatchClass(patchClass.getName(), "finalCampaign.patch.modified.target." + random + "." + targetClass.getName(), loadedProxyPatchClass, proxyPatchClass);
            pool.cache(patchClass.getName(), loadedOriginClass, originClass.toBytecode());

            pool.classPool.clearImportedPackages();

            return originClass;
        }

        pool.classPool.clearImportedPackages();

        return originClass;
    }
}
