package finalCampaign.launch;

import java.io.*;
import java.net.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.transformers.*;

public class shareProvider implements IClassProvider, IClassBytecodeProvider {
    private ClassLoader classLoader;

    public shareProvider() {
        classLoader = Thread.currentThread().getContextClassLoader();
    }

    public void setClassLoader(ClassLoader loader) {
        classLoader = loader;
    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        return classLoader.loadClass(name);
    }

    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, classLoader);
    }

    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return findClass(name, initialize);
    }

    public URL[] getClassPath() {
        ClassLoader systemOne = ClassLoader.getSystemClassLoader();
        return ((URLClassLoader)systemOne).getURLs();
    }

    private byte[] getResourceAsByte(String name) throws IOException {
        InputStream stream = classLoader.getResourceAsStream(name);
        if (stream == null)
            throw new IOException("not found: " + name);
        return shareIOUtil.readAllBytes(stream);
    }

    public ClassNode getClassNode(String className) throws IOException {
        return getClassNode(className, getResourceAsByte(className.replace('.', '/').concat(".class")), ClassReader.EXPAND_FRAMES);
    }
      
    public ClassNode getClassNode(String className, boolean runTransformers) throws IOException {
        return getClassNode(className, getResourceAsByte(className.replace('.', '/').concat(".class")), ClassReader.EXPAND_FRAMES);
    }
      
    private ClassNode getClassNode(String className, byte[] classBytes, int flags) {
        ClassNode classNode = new ClassNode();
        MixinClassReader mixinClassReader = new MixinClassReader(classBytes, className);
        mixinClassReader.accept(classNode, flags);
        return classNode;
    }

}
