package finalCampaign.patch.impl;

import java.net.*;
import org.spongepowered.asm.mixin.*;
import arc.files.*;
import finalCampaign.launch.*;
import mindustry.core.*;

@Mixin(Platform.class)
public interface fcPlatform {
    default ClassLoader loadJar(Fi jar, ClassLoader parent) throws Exception{
        try {
            /*
            Class<?> shareMixinService = Class.forName("finalCampaign.launch.shareMixinService", true, Thread.currentThread().getContextClassLoader());
            Fi mod = (Fi) shareMixinService.getDeclaredField("mod").get(null);
            ClassLoader classLoader = (ClassLoader) shareMixinService.getDeclaredMethod("getClassLoader").invoke(null);
            */
            ClassLoader classLoader = shareMixinService.getClassLoader();
            if (jar.absolutePath().equals(shareMixinService.mod.absolutePath())) return classLoader;
        } catch(Exception ignore) {}

        return new URLClassLoader(new URL[]{jar.file().toURI().toURL()}, parent){
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException{
                //check for loaded state
                Class<?> loadedClass = findLoadedClass(name);
                if(loadedClass == null){
                    try{
                        //try to load own class first
                        loadedClass = findClass(name);
                    }catch(ClassNotFoundException e){
                        //use parent if not found
                        return parent.loadClass(name);
                    }
                }

                if(resolve){
                    resolveClass(loadedClass);
                }
                return loadedClass;
            }
        };
    }
}
