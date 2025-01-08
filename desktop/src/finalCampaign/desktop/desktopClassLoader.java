package finalCampaign.desktop;

import finalCampaign.launch.*;

public class desktopClassLoader extends shareClassLoader {

    protected Class<?> tryLoadClass(String name) throws ClassNotFoundException {
        Class<?> definedClass = null;

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

        return definedClass;
    }
}
