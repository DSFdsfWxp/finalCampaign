package finalCampaign.launch;

import java.io.*;
import java.net.*;
import arc.struct.*;
import arc.util.*;

public abstract class shareClassLoader extends ClassLoader {
    private ClassLoader parent;
    protected Seq<loadedJar> jars;
    protected shareBytecodeTransformer transformer;

    public shareClassLoader() {
        super(new filterClassLoader(shareClassLoader.class.getClassLoader()));
        parent = shareClassLoader.class.getClassLoader();
        jars = new Seq<>();
    }

    public void init() {
        transformer = new shareBytecodeTransformer(shareMixinService.getTransformer());
    }

    public void addJar(fi jarFi) {
        loadedJar jar = new loadedJar();
        
        if (jarFi.isDirectory()) {
            jar.path = "file:/" + jarFi.absolutePath();
            jar.rootDir = jarFi;
        } else {
            jar.path = "jar:file:/" + jarFi.absolutePath() + "!";
            jar.rootDir = new zipFi(jarFi);
        }

        jars.add(jar);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException();
        /*
        try {
            return super.findClass(name);
        } catch(ClassNotFoundException e) {
            if (name.startsWith("java.")) throw new ClassNotFoundException(name);
            return tryLoadClass(name);
        }
         */
    }

    @Override
    public URL getResource(String name) {
        URL url = null;

        for (loadedJar jar : jars) {
            fi f = jar.rootDir;

            if (name.startsWith("/")) name = name.substring(1);
            String[] path = name.split("/");
            for (String n : path) f = f.child(n);

            if (f.exists()) {
                try {
                    url = new URL(jar.path + "/" + name);
                    break;
                } catch(Exception e) {}
            }
        }

        if (url == null)
            url = super.getResource(name);

        return url;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        InputStream stream = null;

        if (OS.isAndroid) {
            for (loadedJar jar : jars) {
                fi f = jar.rootDir;

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
        if (stream == null) stream = shareIOUtil.readFileInternalAsStream(name);
        return stream;
    }

    protected byte[] getResourceAsByte(String name) throws IOException {
        InputStream stream = getResourceAsStream(name);
        return shareIOUtil.readAllBytes(stream);
    }

    public static String getPackageName(String fullClassName) {
        Seq<String> classNames = new Seq<>(fullClassName.split("\\."));
        classNames.pop();
        return String.join(".", classNames);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch(ClassNotFoundException e) {
            if (name.startsWith("java.")) throw new ClassNotFoundException(name);
            Class<?> c = tryLoadClass(name);
            if (c == null) throw new ClassNotFoundException(name);
            if (resolve) super.resolveClass(c);
            return c;
        }
    }

    protected abstract Class<?> tryLoadClass(String name) throws ClassNotFoundException;

    public static class filterClassLoader extends ClassLoader {
        public filterClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith("arc.")) throw new ClassNotFoundException();
            return super.loadClass(name, resolve);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            throw new ClassNotFoundException();
        }
    }

    protected static class loadedJar {
        public String path;
        public fi rootDir;
    }
}
