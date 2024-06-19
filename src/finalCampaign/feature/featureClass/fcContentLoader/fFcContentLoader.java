package finalCampaign.feature.featureClass.fcContentLoader;

import finalCampaign.patch.*;
import finalCampaign.patch.patchClass.*;
import mindustry.core.*;
import mindustry.Vars;

public class fFcContentLoader {

    public static Class<?> contentLoaderClass;

    public static void init() throws Exception {
        contentLoaderClass = modify.patch(fcContentLoader.class, true);
    }

    public static void load() throws Exception {
        Object patchedContentLoader = contentLoaderClass.getDeclaredConstructor(Object.class).newInstance((Object) Vars.content.getContentMap());
        
        Class<?> contentLoaderProxyClass = pool.resolveAllProxiedClass(ContentLoader.class);
        Object contentLoader = contentLoaderProxyClass.getDeclaredConstructor().newInstance();
        
        proxyRuntime.setProxyTarget(contentLoader, patchedContentLoader);
        Vars.content = (ContentLoader) contentLoader;
    }
}
