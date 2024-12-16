package finalCampaign.launch;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import arc.util.*;
import arc.struct.*;
import org.spongepowered.asm.launch.platform.container.*;
import org.spongepowered.asm.logging.*;
import org.spongepowered.asm.mixin.MixinEnvironment.*;
import org.spongepowered.asm.mixin.transformer.*;
import org.spongepowered.asm.service.*;
import com.google.common.collect.*;

public class shareMixinService extends MixinServiceAbstract implements ITransformerProvider {
    private static IMixinTransformer transformer;
    private static shareClassLoader classLoader;
    private static shareProvider provider;

    public static bothFi mod;
    public static bothFi gameJar;
    public static bothFi dataDir;
    public static boolean log = false;
    public static boolean mixinLog = false;
    public static boolean debug = false;
    public static String[] startupArgs;

    private ObjectMap<String, ILogger> loggerMap;

    public static void parseArg(String[] arg) {
        for (String a : arg) {
            if (a.equals("-fcBootstrapLog")) log = true;
            if (a.equals("-fcMixinLog")) mixinLog = true;
            if (a.equals("-fcDebug")) debug = true;
        }

        Seq<String> args = new Seq<>(arg);
        if (log) args.remove("-fcBootstrapLog");
        if (mixinLog) args.remove("-fcMixinLog");
        if (debug) args.remove("-fcDebug");

        startupArgs = args.toArray(String.class);
    }
    
    public shareMixinService() {
        provider = new shareProvider();
    }

    public String getName() {
        return "finalCampaign";
    }

    public boolean isValid() {
        return true;
    }

    public IMixinAuditTrail getAuditTrail() {
        return null;
    }

    public ITransformerProvider getTransformerProvider() {
        return this;
    }

    public IClassTracker getClassTracker() {
        return null;
    }

    public IClassProvider getClassProvider() {
        return provider;
    }

    public IClassBytecodeProvider getBytecodeProvider() {
        return provider;
    }

    public InputStream getResourceAsStream(String name) {
        if (name.startsWith("fcMixin/") && mod != null) {
            bothFi current = new bothZipFi(mod);
            for (String fn : name.split("/")) current = current.child(fn);
            return current.read();
        }

        if (classLoader == null) {
            InputStream stream = shareMixinService.class.getClassLoader().getResourceAsStream(name);
            if (stream == null) stream = shareIOUtil.readFileInternalAsStream(name);
            return stream;
        }

        return classLoader.getResourceAsStream(name);
    }

    public Collection<String> getPlatformAgents() {
        return (Collection<String>)ImmutableList.of("finalCampaign.launch.shareMixinPlatformAgent");
    }

    public IContainerHandle getPrimaryContainer() {
        URI uri = null;

        // the protection domain of the class loaded by dex class loader may always be null.
        if (OS.isAndroid) return (IContainerHandle) new ContainerHandleVirtual(getName());

        try {
            uri = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            if (uri != null)
                return (IContainerHandle) new ContainerHandleURI(uri); 
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return (IContainerHandle) new ContainerHandleVirtual(getName());
    }

    public Collection<ITransformer> getTransformers() {
        return Collections.emptyList();
    }

    public Collection<ITransformer> getDelegatedTransformers() {
        return Collections.emptyList();
    }

    public void addTransformerExclusion(String name) {}

    @Override
    public ILogger getLogger(String name) {
        if (loggerMap == null) loggerMap = new ObjectMap<>();
        ILogger logger = loggerMap.get(name);
        if (logger == null) {
            logger = new shareMixinLogger(name);
            loggerMap.put(name, logger);
        }
        return logger;
    }

    @Override
    public void offer(IMixinInternal internal) {
        // works on 0.8.3 and above
        if (internal instanceof IMixinTransformerFactory)
            transformer = ((IMixinTransformerFactory)internal).createTransformer(); 
        super.offer(internal);
    }

    @Override
    public CompatibilityLevel getMinCompatibilityLevel() {
        return CompatibilityLevel.JAVA_8;
    }

    @SuppressWarnings("unchecked")
    public static IMixinTransformer getTransformer() {
        if (transformer == null) {
            // only works on 0.8 - 0.8.2
            try {
                Constructor<IMixinTransformer> ctor = (Constructor<IMixinTransformer>) Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer").getConstructor(new Class[0]);
                ctor.setAccessible(true);
                transformer = ctor.newInstance(new Object[0]);
            } catch(Exception e) {
                Log.err(e);
            }
        }
        return transformer;
    }

    public static void setClassLoader(shareClassLoader loader) {
        classLoader = loader;
        provider.setClassLoader(loader);
    }

    public static shareClassLoader getClassLoader() {
        return classLoader;
    }

    /** returns the path of GAME jar */
    public static String getClassPath() {
        return gameJar.absolutePath();
    }
}
