package finalCampaign.launch;

import arc.*;
import arc.struct.*;
import arc.files.*;
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
    private volatile ObjectMap<String, Object> map;
    private volatile ObjectMap<String, ClassLoader> loaderMap;
    private Seq<String> loadingClassLst;
    private baseClassLoader loader;
    private DexClassLoader modClassLoader;
    private androidLock mapLock;
    private androidLock loaderMapLock;
    private androidLock loadingLstLock;

    public androidClassLoader(File cacheDir) {
        map = new ObjectMap<>();
        loaderMap = new ObjectMap<>();
        loadingClassLst = new Seq<>();
        loaderMapLock = new androidLock();
        mapLock = new androidLock();
        loadingLstLock = new androidLock();
        loader = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? new inMemoryAndroidClassLoader(this) : new fileAndroidClassLoader(this, cacheDir);
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

    private void beginLoadClass(String name) {
        loadingLstLock.run(() -> {
            loadingClassLst.add(name);
        });
    }

    private void finishLoadClass(String name) {
        loadingLstLock.run(() -> {
            loadingClassLst.remove(name);
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

        // avoid calling load class from sub loaders
        if (loadingClassLst.contains(name)) {
            Log.info("forbidden as loading");
            throw new ClassNotFoundException("forbidden as loading: " + name);
        }

        ClassLoader packageLoader = loaderMap.get(packageName);
        if (packageLoader != null) {
            beginLoadClass(name);
            definedClass = packageLoader.loadClass(name);
            putInMap(name, definedClass);
            finishLoadClass(name);

            Log.info("in package -> " + definedClass.toString());
            return definedClass;
        }

        if (name.startsWith("finalCampaign.") && modClassLoader != null) {
            beginLoadClass(name);
            definedClass = modClassLoader.loadClass(name);
            Log.info("mod loader -> " + name.toString());
            putInMap(name, definedClass);
            finishLoadClass(name);
            return definedClass;
        }

        continuousLoadContext context = new continuousLoadContext(packageName, getAllFilesInJarPath(packageName.replace('.', '/')));
        context.run(transformer);
        ClassLoader cl = loader.loadDex(context.dex);
        putInLoaderMap(packageName, cl);
        beginLoadClass(name);
        definedClass = cl.loadClass(name);
        Log.info("load -> " + definedClass.toString());
        putInMap(name, definedClass);
        finishLoadClass(name);

        throw new ClassNotFoundException(name);
    }

    protected Class<?> platformDefineClass(String name, byte[] bytecode) {
        throw new RuntimeException("Should not go here.");
        /*
        loadingClassLst.add(name);

        Class<?> definedClass = loader.defineClass(name, bytecode);
        putInMap(name, definedClass);

        Log.info("loaded -> " + definedClass.toString());

        loadingClassLst.remove(name);
        return definedClass;
        */
    }

    // load a package in a classloader to avoid illegal access exception happenning between
    // the members in the same package or the same class
    static class continuousLoadContext {
        String packageName;
        Dex dex;
        Seq<Fi> lst;
        Seq<String> classNameLst;
        DexOptions dexOptions;
        CfOptions cfOptions;

        public continuousLoadContext(String PackageName, Seq<Fi> classLst) {
            packageName = PackageName;
            lst = new Seq<>();
            classNameLst = new Seq<>();
            dexOptions = new DexOptions();
            dexOptions.minSdkVersion = Core.app.getVersion();
            cfOptions = new CfOptions();

            for (Fi f : classLst) if (f.extension().equals("class")) lst.add(f);
            for (Fi f : lst) classNameLst.add(f.nameWithoutExtension());
        }

        public void run(shareBytecodeTransformer transformer) {
            for (int i=0; i<classNameLst.size; i++) {
                String name = classNameLst.get(i);
                String fullName = packageName + "." + name;
                Fi file = lst.get(i);
                byte[] classBytecode = transformer.transform(fullName, file == null ? null : file.readBytes());

                Log.info(String.format("loading : %s , %d", fullName, classBytecode == null ? -1 : classBytecode.length));

                bothClassPatcher reader = new bothClassPatcher(classBytecode);
                for (bothClassPatcher.constentPoolItem item : reader.constentItems) {
                    if (item.tag != 7 && item.tag != 12) continue;
                    Seq<String> classesNeedToParse = new Seq<>();

                    if (item.tag == 7) { // class ref
                        classesNeedToParse.add(reader.constentItems[item.pos1].string);
                    } else { // name and type desc
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
                            if (pos > -1) continue;
                            classNameLst.add(className);
                            lst.add((Fi) null);
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
        public abstract ClassLoader loadDex(Dex dex);
    }

    static class fileAndroidClassLoader extends baseClassLoader {
        private File cacheDir;
        private ClassLoader parent;
        private static long id = 0;

        public fileAndroidClassLoader(ClassLoader parent, File cacheDir) {
            this.parent = parent;
            this.cacheDir = cacheDir;
            cacheDir.mkdirs();
        }

        public ClassLoader loadDex(Dex dex) {
            id ++;
            File dexFile = new File(cacheDir, id + ".dex");

            try{
                dex.writeTo(dexFile);
            }catch(IOException e){
                e.printStackTrace();
            }
            android.content.Context context = (android.content.Context) Core.app;
            ClassLoader res = new DexClassLoader(dexFile.getPath(), VERSION.SDK_INT >= 21 ? context.getCodeCacheDir().getPath() : context.getCacheDir().getAbsolutePath(), null, parent);

            dexFile.delete();
            return res;
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    static class inMemoryAndroidClassLoader extends baseClassLoader {
        private ClassLoader parent;

        public inMemoryAndroidClassLoader(ClassLoader parent) {
            this.parent = parent;
        }

        public ClassLoader loadDex(Dex dex) {
            return new InMemoryDexClassLoader(ByteBuffer.wrap(dex.getBytes()), parent);
        }
    }
}
