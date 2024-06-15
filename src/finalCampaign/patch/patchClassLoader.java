package finalCampaign.patch;

import java.io.*;
import java.nio.charset.*;
import java.security.*;
import java.util.zip.*;
import arc.files.*;
import arc.struct.*;
import javassist.*;

import static mindustry.Vars.*;

public class patchClassLoader extends ClassLoader {
    public patchClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Class<?> loadCtClass(CtClass cclass) throws IOException, CannotCompileException, NoSuchAlgorithmException, Exception {
        byte[] code = cclass.toBytecode();
        String type, patchClassShortHashName = null, targetClassName;

        Seq<String> splited = util.subSeq(new Seq<>(cclass.getName().split("\\.")), 2);
        
        type = util.shiftSeq(splited) + "." + util.shiftSeq(splited);
        if (!type.equals("proxied.all")) patchClassShortHashName = util.shiftSeq(splited);
        targetClassName = String.join(".", splited);

        if (!android) {
            cache.write(type, patchClassShortHashName, targetClassName, code, null);
            return loadClassBinary(cclass.getName() ,code);
        }

        dexFile dex = new dexFile();
        dex.addClass(cclass.getName(), code);
        byte[] dexCode = dex.toByte();

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ZipOutputStream stream = new ZipOutputStream(byteStream);

        stream.putNextEntry(new ZipEntry("META-INF/"));
        stream.closeEntry();
        stream.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
        stream.write("Manifest-Version: 1.0\n\n".getBytes(StandardCharsets.UTF_8));
        stream.closeEntry();
        stream.putNextEntry(new ZipEntry("classes.dex"));
        stream.write(dexCode);
        stream.closeEntry();

        stream.close();

        cache.write(type, patchClassShortHashName, targetClassName, code, dexCode);
        return cache.resolve(type, patchClassShortHashName, targetClassName);
    }

    public Class<?> loadClassBinary(String name, byte[] bin) {
        return defineClass(name, bin, 0, bin.length);
    }

    public Class<?> loadDexFile(String name, Fi file) throws Exception {
        ClassLoader loader = platform.loadJar(file, this);
        return Class.forName(name, true, loader);
    }
}
