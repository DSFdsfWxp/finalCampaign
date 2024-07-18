package finalCampaign.launch;

import arc.struct.*;

public class desktopClassLoader extends shareClassLoader {
    private volatile ObjectMap<String, Class<?>> map;
    private shareLock lock;

    public desktopClassLoader() {
        map = new ObjectMap<>();
        lock = new shareLock();
    }

    private void putInMap(String name, Class<?> c) {
        lock.run(() -> {
            map.put(name, c);
        });
    }

    protected Class<?> tryLoadClass(String name) throws ClassNotFoundException {
        Class<?> definedClass = map.get(name);
        if (definedClass != null) return definedClass;

        String classPath = name.replace('.', '/').concat(".class");
        
        try {
            byte[] originBytecode = null;

            try {
                originBytecode = getResourceAsByte(classPath);
            } catch(Exception ignore) {}

            if (transformer == null) throw new ClassNotFoundException();
            byte[] transformedBytecode = transformer.transform(name, originBytecode);

            if (transformedBytecode == null) throw new ClassNotFoundException();
            definedClass = defineClass(name, transformedBytecode, 0, transformedBytecode.length);
        } catch(Exception e) {
            throw new ClassNotFoundException("try load class failed: " + name, e);
        }

        putInMap(name, definedClass);
        return definedClass;
    }
}
