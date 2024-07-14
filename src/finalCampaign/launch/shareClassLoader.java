package finalCampaign.launch;

import java.io.*;
import java.net.*;
import arc.files.*;
import arc.struct.*;
import arc.util.*;

public abstract class shareClassLoader extends ClassLoader {
    private ClassLoader parent;
    private Seq<Fi> jars;
    protected shareBytecodeTransformer transformer;

    public shareClassLoader() {
        super(shareClassLoader.class.getClassLoader());
        parent = shareClassLoader.class.getClassLoader();
        jars = new Seq<>();
    }

    public void init() {
        transformer = new shareBytecodeTransformer(shareMixinService.getTransformer());
    }

    public void addJar(Fi jarFi) {
        jars.add(jarFi);
    }

    protected abstract Class<?> platformDefineClass(String name, byte[] bytecode);

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
           return super.findClass(name);
        } catch(ClassNotFoundException e) {
            return tryLoadClass(name);
        }
    }

    @Override
    public URL getResource(String name) {
        URL url = super.getResource(name);
        if (url == null) {
            for (Fi fi : jars) {
                Fi f = new ZipFi(fi);

                if (name.startsWith("/")) name = name.substring(1);
                String[] path = name.split("/");
                for (String n : path) f = f.child(n);

                if (f.exists()) {
                    try {
                        url = new URL("jar:file:/" + fi.toString() + "!/" + name);
                        break;
                    } catch(Exception e) {}
                }
            }
        }
        return url;
    }

    protected Seq<Fi> getAllFilesInJarPath(String path) {
        Seq<Fi> res = new Seq<>();
        for (Fi fi : jars) {
            Fi f = new ZipFi(fi);

            if (path.startsWith("/")) path = path.substring(1);
            if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
            String[] paths = path.split("/");
            for (String n : paths) f = f.child(n);

            if (f.exists()) {
                try {
                    if (f.isDirectory()) for (Fi ff : f.list()) if (!ff.isDirectory()) res.add(ff);
                    break;
                } catch(Exception e) {
                    Log.err(e);
                }
            }
        }
        return res;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        InputStream stream = null;

        if (OS.isAndroid) {
            for (Fi fi : jars) {
                Fi f = new ZipFi(fi);

                if (name.startsWith("/")) name = name.substring(1);
                String[] path = name.split("/");
                for (String n : path) f = f.child(n);

                if (f.exists()) {
                    stream = f.read();
                    break;
                }
            }
        } else {
            stream = super.getResourceAsStream(name);
        }

        if (stream == null) stream = parent.getResourceAsStream(name);
        if (stream == null) stream = bothIOUtil.readFileInternalAsStream(name);
        return stream;
    }

    protected byte[] getResourceAsByte(String name) throws IOException {
        InputStream stream = getResourceAsStream(name);
        return bothIOUtil.readAllBytes(stream);
    }

    public String getPackageName(String fullClassName) {
        Seq<String> classNames = new Seq<>(fullClassName.split("\\."));
        classNames.pop();
        return String.join(".", classNames);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch(ClassNotFoundException e) {
            Class<?> c = tryLoadClass(name);
            if (c == null) throw new ClassNotFoundException(name);
            if (resolve) super.resolveClass(c);
            return c;
        }
    }

    protected Class<?> tryLoadClass(String name) throws ClassNotFoundException {
        if (name.startsWith("java.")) return null;
        String classPath = name.replace('.', '/').concat(".class");
        
        try {
            byte[] originBytecode = null;

            try {
                originBytecode = getResourceAsByte(classPath);
            } catch(Exception ignore) {}

            if (transformer == null) throw new ClassNotFoundException();
            byte[] transformedBytecode = transformer.transform(name, originBytecode);

            if (transformedBytecode == null) throw new ClassNotFoundException();
            return platformDefineClass(name, transformedBytecode);
        } catch(Exception e) {
            throw new ClassNotFoundException("try load class failed: " + name, e);
        }
    }
}
