package finalCampaign.launch;

import java.io.*;
import java.net.*;
import arc.struct.*;
import arc.util.*;

public abstract class shareClassLoader extends ClassLoader {
    private ClassLoader parent;
    private Seq<shareFi> jars;
    protected shareBytecodeTransformer transformer;

    public shareClassLoader() {
        super(shareClassLoader.class.getClassLoader());
        parent = shareClassLoader.class.getClassLoader();
        jars = new Seq<>();
    }

    public void init() {
        transformer = new shareBytecodeTransformer(shareMixinService.getTransformer());
    }

    public void addJar(shareFi jarFi) {
        jars.add(jarFi);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            Class<?> res = findLoadedClass(name);
            if (res == null) throw new ClassNotFoundException();
            return res;
        } catch(ClassNotFoundException e) {
            if (name.startsWith("java.")) throw new ClassNotFoundException(name);
            return tryLoadClass(name);
        }
    }

    @Override
    public URL getResource(String name) {
        URL url = super.getResource(name);
        if (url == null) {
            for (shareFi fi : jars) {
                shareFi f = new shareZipFi(fi);

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

    protected Seq<shareFi> getAllFilesInJarPath(String path) {
        Seq<shareFi> res = new Seq<>();
        for (shareFi fi : jars) {
            shareFi f = new shareZipFi(fi);

            if (path.startsWith("/")) path = path.substring(1);
            if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
            String[] paths = path.split("/");
            for (String n : paths) f = f.child(n);

            if (f.exists()) {
                try {
                    if (f.isDirectory()) for (shareFi ff : f.list()) if (!ff.isDirectory()) res.add(ff);
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
            for (shareFi fi : jars) {
                shareFi f = new shareZipFi(fi);

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
            if (name.startsWith("arc.")) {
                try {
                    Class<?> c = parent.loadClass(name);
                    if (c.getClassLoader().equals(this)) return c;
                } catch(Exception ignore) {}
            }
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
}
