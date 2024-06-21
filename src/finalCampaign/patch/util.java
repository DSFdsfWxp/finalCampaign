package finalCampaign.patch;

import java.nio.charset.*;
import java.security.*;
import arc.struct.*;
import finalCampaign.patch.annotation.*;
import javassist.*;

public class util {

    public static enum patchOperation {
        Access,
        Add,
        Replace,
        SuperCall,
        None
    }

    public static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static <T> Seq<T> subSeq(Seq<T> seq, int start) {
        Seq<T> out = new Seq<>();
        for (T e : seq) {
            if (start == 0) {
                out.add(e);
            } else {
                start --;
            }
        }

        return out;
    }

    public static <T> T shiftSeq(Seq<T> seq) {
        T out = seq.first();
        
        seq.remove(0);
        return out;
    }

    public static String hash(String str) {
        int hashCode = str.hashCode();
        String hashStr = Integer.toString(Math.abs(hashCode));
        return hashCode > 0 ? "p" + hashStr : "n" + hashStr;
    }

    public static String shortHashName(String name) {
        if (name == null) return null;
        // since we put all patch classes in "finalCampaign.patch.patchClass"
        // we'll remove it here
        if (!name.startsWith("finalCampaign.patch.patchClass.")) throw new RuntimeException("Short hash name is not for that.");
        name = name.substring(31);

        return hash(name);
    }

    public static String nameBuilder(String type, String patchClassHashName, String targetClassName) {
        final String spliter = "$";
        String shortName = spliter + "finalCampaign" + spliter + "patch" + spliter + type.replace(".", spliter);
        if (patchClassHashName != null) shortName += spliter + patchClassHashName;
        return targetClassName + shortName;
    }

    public static String[] nameDisassembler(String name) {
        final String spliter = "$";
        final String escapedSpliter = "\\$";
        Seq<String> splited = new Seq<>(name.split(escapedSpliter + "finalCampaign" + escapedSpliter + "patch" + escapedSpliter));
        String tmp = splited.pop();
        String targetClassName = String.join(spliter + "finalCampaign" + spliter + "patch" + spliter, splited);
        splited = new Seq<>(tmp.split(escapedSpliter));
        String patchClassShortHashName = null;
        if (splited.size > 2) patchClassShortHashName = splited.pop();
        String type = String.join(".", splited);
        return new String[]{type, patchClassShortHashName, targetClassName};
    }

    public static String sha256Hash(String txt) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] hash = messageDigest.digest(txt.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    public static String repeatString(String txt, int count) {
        String out = "";
        for (int i=0; i<count; i++) out += txt;
        return out;
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
        if (annotation instanceof PatchSuperCall) return patchOperation.SuperCall;
        
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

    public static String getTypeExpression(String rawTypeName) {
        if (!rawTypeName.startsWith("[")) return rawTypeName;

        int arrayNum = 0;
        String[] splited = rawTypeName.split("");
        for (String t : splited) {
            if (!t.equals("[")) break;
            arrayNum ++;
        }

        String typeName = rawTypeName.substring(arrayNum + 1);
        switch (rawTypeName.substring(arrayNum - 1, arrayNum + 1)) {
            case "[I":
                typeName = "int";
                break;
            case "[Z":
                typeName = "boolean";
                break;
            case "[B":
                typeName = "byte";
                break;
            case "[S":
                typeName = "short";
                break;
            case "[J":
                typeName = "long";
                break;
            case "[F":
                typeName = "float";
                break;
            case "[D":
                typeName = "double";
                break;
            case "[C":
                typeName = "char";
                break;
            case "[L":
                typeName = typeName.substring(0, typeName.length() - 1);
                break;
            default:
                throw new RuntimeException("Should not reach here.");
        }

        return typeName + repeatString("[]", arrayNum);
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
            if (field.getName().equals(name)) return true;
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

    public static CtField[] getAllDeclaredFields(CtClass tClass) throws NotFoundException {
        Seq<CtField> out = new Seq<>();
        CtClass current = tClass;
        while (!current.getName().equals("java.lang.Object")) {
            out.add(current.getDeclaredFields());
            current = current.getSuperclass();
        }

        return out.toArray(CtField.class);
    }

    public static CtMethod[] getAllDeclaredMethods(CtClass tClass) throws NotFoundException {
        Seq<CtMethod> out = new Seq<>();
        CtClass current = tClass;
        while (!current.getName().equals("java.lang.Object")) {
            out.add(current.getDeclaredMethods());
            current = current.getSuperclass();
        }

        return out.toArray(CtMethod.class);
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
        target.setSuperclass(pool.resolveCtClass("java.lang.Object"));

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
