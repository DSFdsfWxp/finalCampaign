package finalCampaign.patch;

import arc.struct.*;
import finalCampaign.patch.annotation.*;
import javassist.*;

public class util {

    public static enum patchOperation {
        Access,
        Add,
        Replace,
        None
    }

    public static String randomName(int length) {
        final String[] map = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".split("");
        final String number = "1234567890";
        String out = "";

        while (out.length() < length) {
            int pos = (int) Math.floor(Math.random() * map.length);
            String txt = map[pos];
            
            if (out.length() == 0 && number.contains(txt)) continue;
            out += txt;
        }

        return out;
    }

    public static String randomName() {
        return randomName(8);
    }

    public static Seq<Object> getPatchAnnotations(Seq<Object> annotations) {
        Seq<Object> patchAnnotations = new Seq<>();

        for (Object annotation : annotations) {
            if (annotation instanceof PatchAccess ||
                annotation instanceof PatchAdd ||
                annotation instanceof PatchModify ||
                annotation instanceof PatchProxy ||
                annotation instanceof PatchReplace ||
                annotation instanceof PatchImport) patchAnnotations.add(annotation);
        }
        
        return patchAnnotations;
    }

    public static patchOperation getPatchOperation(CtMember member) throws ClassNotFoundException {
        Seq<Object> annotations = getPatchAnnotations(new Seq<Object>(member.getAnnotations()));

        if (annotations.size == 0) return patchOperation.None;
        if (annotations.size > 1) throw new RuntimeException("A class member with multi annotations is not allowed.");
        
        Object annotation = annotations.get(0);
        if (annotation instanceof PatchProxy || annotation instanceof PatchModify) throw new RuntimeException("Class members can not be patched.");

        if (annotation instanceof PatchAdd) return patchOperation.Add;
        if (annotation instanceof PatchAccess) return patchOperation.Access;
        if (annotation instanceof PatchReplace) return patchOperation.Replace;
        
        throw new RuntimeException("Should not reach here.");
    }

    public static String[] getPatchImport(CtClass patchClass) throws ClassNotFoundException {
        Seq<Object> annotations = getPatchAnnotations(new Seq<Object>(patchClass.getAnnotations()));

        for (Object annotation : annotations) {
            if (annotation instanceof PatchImport patchImportAnnotation) return patchImportAnnotation.value();
        }

        return new String[]{};
    }

    public static String getRealClassName(String simpleName) {
        Seq<String> t = new Seq<>(simpleName.split(","));
        return t.pop();
    }

    public static CtClass getNestClass(CtClass tClass, String name) throws NotFoundException {
        CtClass[] lst = tClass.getDeclaredClasses();
        for (CtClass cclass : lst) {
            if (getRealClassName(cclass.getSimpleName()).equals(name)) return cclass;
        }

        throw new RuntimeException("There is not a nest class called \"" + name + "\".");
    }

    public static boolean hasField(CtClass tClass, String name) {
        CtField[] lst = tClass.getDeclaredFields();

        for (CtField field : lst) {
            if (!field.getName().equals(name)) return true;
        }

        return false;
    }

    public static boolean hasBehavior(CtClass tClass, String name, CtClass[] parameter) throws NotFoundException {
        CtBehavior[] lst = tClass.getDeclaredBehaviors();

        for (CtBehavior behavior : lst) {
            if (!behavior.getName().equals(name)) continue;

            CtClass[] bParameter = behavior.getParameterTypes();
            if (bParameter.length != parameter.length) continue;

            boolean shouldPass = false;
            for (int i=0; i<parameter.length; i++){
                if (!bParameter[i].equals(parameter[i])) {
                    shouldPass = true;
                    break;
                }
            }

            if (shouldPass) continue;

            return true;
        }

        return false;
    }

    public static boolean hasNestClass(CtClass tClass, String name) throws NotFoundException {
        CtClass[] lst = tClass.getDeclaredClasses();
        name = getRealClassName(name);

        for (CtClass cclass : lst) {
            if (getRealClassName(cclass.getSimpleName()).equals(name)) return true;
        }

        return false;
    }

    public static void copyClass(CtClass source, CtClass dest) throws CannotCompileException, NotFoundException {
        dest.setInterfaces(source.getInterfaces());
        dest.setModifiers(source.getModifiers());
        dest.setSuperclass(source.getSuperclass());

        CtField[] sFields = source.getDeclaredFields();
        for (CtField sField : sFields) {
            CtField newField = new CtField(sField, dest);
            newField.setModifiers(sField.getModifiers());
            dest.addField(newField);
        }

        CtConstructor[] sConstructors = source.getDeclaredConstructors();
        for (CtConstructor sConstructor : sConstructors) {
            CtConstructor newConstructor = new CtConstructor(sConstructor, dest, null);
            newConstructor.setModifiers(sConstructor.getModifiers());
            dest.addConstructor(newConstructor);
        }

        CtMethod[] sMethods = source.getDeclaredMethods();
        for (CtMethod sMethod : sMethods) {
            CtMethod newMethod = new CtMethod(sMethod, dest, null);
            newMethod.setModifiers(sMethod.getModifiers());
            dest.addMethod(newMethod);
        }

        CtClass[] sClasses = source.getDeclaredClasses();
        for (CtClass sClass : sClasses) {
            String className = getRealClassName(sClass.getSimpleName());
            if (hasNestClass(dest, className)) {
                copyClass(sClass, clearClass(getNestClass(dest, className)));
            } else {
                copyClass(sClass, dest.makeNestedClass(className, Modifier.isStatic(sClass.getModifiers())));
            }
        }
    }

    public static CtClass clearClass(CtClass target) throws CannotCompileException, NotFoundException {
        target.setInterfaces(new CtClass[]{});
        target.setModifiers(Modifier.PUBLIC);
        target.setSuperclass(pool.classPool.get("java.lang.Object"));

        CtField[] tFields = target.getDeclaredFields();
        for (CtField tField : tFields) {
            target.removeField(tField);
        }

        CtConstructor[] tConstructors = target.getDeclaredConstructors();
        for (CtConstructor tConstructor : tConstructors) {
            target.removeConstructor(tConstructor);
        }

        CtMethod[] tMethods = target.getDeclaredMethods();
        for (CtMethod tMethod : tMethods) {
            target.removeMethod(tMethod);
        }

        CtClass[] tClasses = target.getDeclaredClasses();
        for (CtClass tClass : tClasses) {
            clearClass(tClass);
        }

        return target;
    }
}
