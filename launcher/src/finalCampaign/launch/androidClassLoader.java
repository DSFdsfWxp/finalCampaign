package finalCampaign.launch;

import arc.struct.*;
import arc.util.Log;
import android.annotation.*;
import android.os.*;
import android.os.Build.*;
import java.io.*;
import java.nio.*;
import finalCampaign.com.android.dex.*;
import finalCampaign.com.android.dx.cf.direct.*;
import finalCampaign.com.android.dx.command.dexer.*;
import finalCampaign.com.android.dx.dex.*;
import finalCampaign.com.android.dx.dex.cf.*;
import finalCampaign.com.android.dx.dex.file.DexFile;
import finalCampaign.com.android.dx.merge.*;
import dalvik.system.*;

public class androidClassLoader extends shareClassLoader {
    // thread safty for pathfinding thread of mindustry, settings backup thread of arc, and other...
    private volatile ObjectMap<String, Object> map;
    private volatile ObjectMap<String, ClassLoader> loaderMap;

    private shareLock mapLock;
    private shareLock loaderMapLock;

    private baseClassLoader loader;
    private DexClassLoader modClassLoader;

    public androidClassLoader(File cacheDir, File codeCacheDir, String nativveLibPath) {
        map = new ObjectMap<>();
        loaderMap = new ObjectMap<>();
        loaderMapLock = new shareLock();
        mapLock = new shareLock();
        // try our best to make it compatible with api level 14
        loader = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? new inMemoryAndroidClassLoader(this, nativveLibPath) : new fileAndroidClassLoader(this, cacheDir, codeCacheDir);
    }

    public void createModClassLoader(androidLauncher application) {
        modClassLoader = new DexClassLoader(shareMixinService.mod.absolutePath(), application.getFilesDir().getPath(), null, this) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException{
                //check for loaded state
                Class<?> loadedClass = findLoadedClass(name);
                if(loadedClass == null){
                    try{
                        //try to load own class first
                        loadedClass = findClass(name);
                    }catch(ClassNotFoundException | NoClassDefFoundError e){
                        //use parent if not found
                        return this.getParent().loadClass(name);
                    }
                }

                if(resolve){
                    resolveClass(loadedClass);
                }
                return loadedClass;
            }
        };
    }

    @SuppressWarnings("unchecked")
    protected void dumpMap(Seq<String> stack, ObjectMap<String, Object> map) {
        for (String key : map.keys()) {
            Object v = map.get(key);
            stack.add(key);
            try {
                ObjectMap<String, Object> subMap = (ObjectMap<String, Object>) v;
                dumpMap(stack, subMap);
            } catch(Exception e) {
                Log.info(String.join("/", stack) + " -> " + v.toString());
            }
            stack.remove(key);
        }
    }

    @SuppressWarnings("unchecked")
    private void putInMap(String name, Class<?> c) {
        mapLock.run(() -> {
            String n = name;
            Seq<String> stack = new Seq<>(name.split("\\."));
            n = stack.pop();
            ObjectMap<String, Object> current = map;
            for (String sub : stack) {
                ObjectMap<String, Object> next = (ObjectMap<String, Object>) current.get(sub);
                if (next == null) {
                    next = new ObjectMap<>();
                    current.put(sub, next);
                }
                current = next;
            }
            current.put(n, c);
        });
    }

    @SuppressWarnings("unchecked")
    private Class<?> getFromMap(String name) {
        Seq<String> stack = new Seq<>(name.split("\\."));
        name = stack.pop();
        ObjectMap<String, Object> current = map;
        for (String sub : stack) {
            ObjectMap<String, Object> next = (ObjectMap<String, Object>) current.get(sub);
            if (next == null) return null;
            current = next;
        }
        return (Class<?>) current.get(name);
    }

    private void putInLoaderMap(String packageName, ClassLoader classLoader) {
        loaderMapLock.run(() -> {
            loaderMap.put(packageName, classLoader);
        });
    }

    @Override
    protected Class<?> tryLoadClass(String name) throws ClassNotFoundException {
        Log.info("cl req: " + name);
        String packageName = getPackageName(name);

        Class<?> definedClass = getFromMap(name);
        if (definedClass != null) {
            Log.info("in map -> " + definedClass.toString());
            return definedClass;
        }

        ClassLoader packageLoader = loaderMap.get(packageName);
        if (packageLoader != null) {
            definedClass = packageLoader.loadClass(name);
            putInMap(name, definedClass);

            Log.info("in package -> " + definedClass.toString());
            return definedClass;
        }

        // finalCampaign.patch.* are used for mixin
        // it needs java bytecode instead of class object
        if (name.startsWith("finalCampaign.") && !name.startsWith("finalCampaign.patch.") && modClassLoader != null) {
            definedClass = modClassLoader.loadClass(name);
            Log.info("mod loader -> " + name.toString());
            putInMap(name, definedClass);
            return definedClass;
        }

        continuousLoadContext context = new continuousLoadContext(packageName, getAllFilesInJarPath(packageName.replace('.', '/')));
        context.run(transformer);
        ClassLoader cl = loader.loadDex(context.dex, packageName);
        putInLoaderMap(packageName, cl);
        definedClass = cl.loadClass(name);
        Log.info("load -> " + definedClass.toString());
        putInMap(name, definedClass);

        return definedClass;
    }

    // load a package in a classloader to avoid illegal access exception happenning between
    // the members in the same package or the same class
    // we can not load all class in one classloader like what we do on desktop due to android's restrictions
    static class continuousLoadContext {
        String packageName;
        Dex dex;
        Seq<fi> lst;
        Seq<String> classNameLst;
        DexOptions dexOptions;
        CfOptions cfOptions;

        public continuousLoadContext(String PackageName, Seq<fi> classLst) {
            packageName = PackageName;
            lst = new Seq<>();
            classNameLst = new Seq<>();
            dexOptions = new DexOptions();
            dexOptions.minSdkVersion = android.os.Build.VERSION.SDK_INT;
            cfOptions = new CfOptions();

            for (fi f : classLst) {
                if (f.extension().equals("class") && classNameLst.indexOf(f.nameWithoutExtension()) == -1) {
                    lst.add(f);
                    classNameLst.add(f.nameWithoutExtension());
                }
            }
        }

        public void run(shareBytecodeTransformer transformer) {
            for (int i=0; i<classNameLst.size; i++) {
                String name = classNameLst.get(i);
                String fullName = packageName + "." + name;
                fi file = lst.get(i);

                byte[] classBytecode = transformer.transform(fullName, file == null ? null : file.readBytes());

                // resolve synthetic classes which are renamed and injected by mixin and are needed to be generated dynamic
                androidClassPatcher reader = new androidClassPatcher(classBytecode);
                for (androidClassPatcher.constentPoolItem item : reader.constentItems) {
                    if (item.tag != 7 && item.tag != 12) continue;
                    Seq<String> classesNeedToParse = new Seq<>();

                    if (item.tag == 7) { // class ref
                        classesNeedToParse.add(reader.constentItems[item.pos1].string);
                    } else { // name and type descriptor
                        String tmp = null;
                        String src = reader.constentItems[item.pos2].string;
                        for (int ii=0; ii<src.length(); ii++) {
                            switch (src.charAt(ii)) {
                                case 'L': {
                                    if (tmp == null) {
                                        tmp = "";
                                    } else {
                                        tmp += "L";
                                    }
                                    break;
                                }
                                case ';': {
                                    if (tmp != null) {
                                        classesNeedToParse.add(tmp);
                                        tmp = null;
                                    }
                                    break;
                                }
                                default: {
                                    if (tmp != null) tmp += src.charAt(ii);
                                }
                            }
                        }
                    }

                    for (String classPath : classesNeedToParse) {
                        Seq<String> classPaths = new Seq<>(classPath.split("/"));
                        if (classPaths.size < 1) continue;
                        String className = classPaths.pop();
                        if (String.join(".", classPaths).equals(packageName)) {
                            int pos = classNameLst.indexOf(className);
                            // skip existed one
                            // simply make sure it's what we need
                            if (pos > -1) continue;
                            classNameLst.add(className);
                            lst.add((fi) null);
                        }
                    }
                }

                try {
                    DexFile dexFile = new DexFile(dexOptions);
                    DirectClassFile classFile = new DirectClassFile(classBytecode, fullName.replace('.', '/') + ".class", true);
                    classFile.setAttributeFactory(StdAttributeFactory.THE_ONE);
                    classFile.getMagic();
                    DxContext context = new DxContext();
                    dexFile.add(CfTranslator.translate(context, classFile, null, cfOptions, dexOptions, dexFile));
                    Dex newDex = new Dex(dexFile.toDex(null, false));
                    // merge them up because one classloader only accepts *one* dex file
                    if (dex == null) {
                        dex = newDex;
                    } else {
                        dex = (new DexMerger(new Dex[] {newDex, dex}, CollisionPolicy.KEEP_FIRST, context)).merge();
                    }
                } catch(IOException e) {
                    throw new RuntimeException("Failed to define class", e);
                }
            }
        }
    }

    static abstract class baseClassLoader {
        public abstract ClassLoader loadDex(Dex dex, String packageName);
    }

    // class loader from mindustry src: AndroidRhinoContext
    static class fileAndroidClassLoader extends baseClassLoader {
        private File cacheDir;
        private File codeCacheDir;
        private ClassLoader parent;
        private static long id = 0;

        public fileAndroidClassLoader(ClassLoader parent, File cacheDir, File codeCacheDir) {
            this.parent = parent;
            this.cacheDir = cacheDir;
            this.codeCacheDir = codeCacheDir;
            cacheDir.mkdirs();
        }

        public ClassLoader loadDex(Dex dex, String packageName) {
            id ++;
            File dexFile = new File(cacheDir, id + ".dex");

            try{
                dex.writeTo(dexFile);
            }catch(IOException e){
                e.printStackTrace();
            }

            ClassLoader res = new DexClassLoader(dexFile.getPath(), VERSION.SDK_INT >= 21 ? codeCacheDir.getPath() : cacheDir.getAbsolutePath(), null, parent);

            dexFile.delete();
            return res;
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    static class inMemoryAndroidClassLoader extends baseClassLoader {
        private ClassLoader parent;
        private String nativeLibPath;

        public inMemoryAndroidClassLoader(ClassLoader parent, String nativeLibPath) {
            this.parent = parent;
            this.nativeLibPath = nativeLibPath;
        }

        public ClassLoader loadDex(Dex dex, String packageName) {
            return new InMemoryDexClassLoader(new ByteBuffer[]{ByteBuffer.wrap(dex.getBytes())}, nativeLibPath + File.pathSeparator, new ClassLoader(parent) {
                @Override
                protected Class<?> findClass(String name) throws ClassNotFoundException {
                    Log.info(String.format("find %s in %s", name, packageName));
                    // when invoke findClass or loadClass in a classloader, it'll find or load from it's parent
                    // and inMemoryAndroidClassLoader is a *final* class
                    if (shareClassLoader.getPackageName(name).equals(packageName)) throw new ClassNotFoundException("Load it by youself: " + name);
                    return super.findClass(name);
                }

                @Override
                protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                    Log.info(String.format("load %s in %s", name, packageName));
                    if (shareClassLoader.getPackageName(name).equals(packageName)) throw new ClassNotFoundException("Load it by youself: " + name);
                    return super.loadClass(name, false);
                }
            });
        }
    }
}
