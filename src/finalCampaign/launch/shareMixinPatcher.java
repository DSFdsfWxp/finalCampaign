package finalCampaign.launch;

import java.lang.reflect.*;
import org.spongepowered.asm.util.asm.*;
import arc.util.*;

public class shareMixinPatcher {

    public static int majorVersion = 9;
    public static int minorVersion = 0;
    
    // Implementation versions, only available from ASM
    public static int implMinorVersion = 0;
    public static int patchVersion = 0;
    
    public static String maxVersion = "ASM10_EXPERIMENTAL";
    
    public static int maxClassVersion = 0;
    public static int maxClassMajorVersion = 65;
    public static int maxClassMinorVersion = 0;
    public static String maxJavaVersion = "21";

    public static int API_VERSION = 0x90000;

    public static void read() {
        try {
            Field majorVersion = ASM.class.getDeclaredField("majorVersion");
            Field minorVersion = ASM.class.getDeclaredField("minorVersion");
            Field implMinorVersion = ASM.class.getDeclaredField("implMinorVersion");
            Field patchVersion = ASM.class.getDeclaredField("patchVersion");
            Field maxVersion = ASM.class.getDeclaredField("maxVersion");
            Field maxClassVersion = ASM.class.getDeclaredField("maxClassVersion");
            Field maxClassMajorVersion = ASM.class.getDeclaredField("maxClassMajorVersion");
            Field maxClassMinorVersion = ASM.class.getDeclaredField("maxClassMinorVersion");
            Field maxJavaVersion = ASM.class.getDeclaredField("maxJavaVersion");
            Field API_VERSION = ASM.class.getDeclaredField("API_VERSION");

            majorVersion.setAccessible(true);
            minorVersion.setAccessible(true);
            implMinorVersion.setAccessible(true);
            patchVersion.setAccessible(true);
            maxVersion.setAccessible(true);
            maxClassVersion.setAccessible(true);
            maxClassMajorVersion.setAccessible(true);
            maxClassMinorVersion.setAccessible(true);
            maxJavaVersion.setAccessible(true);
            API_VERSION.setAccessible(true);

            shareMixinPatcher.majorVersion = majorVersion.getInt(null);
            shareMixinPatcher.minorVersion = minorVersion.getInt(null);
            shareMixinPatcher.implMinorVersion = implMinorVersion.getInt(null);
            shareMixinPatcher.patchVersion = patchVersion.getInt(null);
            shareMixinPatcher.maxVersion = (String) maxVersion.get(null);
            shareMixinPatcher.maxClassMajorVersion = maxClassMajorVersion.getInt(null);
            shareMixinPatcher.maxClassMinorVersion = maxClassMinorVersion.getInt(null);
            shareMixinPatcher.maxJavaVersion = (String) maxJavaVersion.get(null);
            shareMixinPatcher.API_VERSION = API_VERSION.getInt(null);

        } catch(Exception e) {
            Log.err(e);
        }
    }

    public static void patch() {
        try {
            ASM.getMaxSupportedClassVersionMajor();
        } catch(Exception ignore) {}

        try {
            Field majorVersion = ASM.class.getDeclaredField("majorVersion");
            Field minorVersion = ASM.class.getDeclaredField("minorVersion");
            Field implMinorVersion = ASM.class.getDeclaredField("implMinorVersion");
            Field patchVersion = ASM.class.getDeclaredField("patchVersion");
            Field maxVersion = ASM.class.getDeclaredField("maxVersion");
            Field maxClassVersion = ASM.class.getDeclaredField("maxClassVersion");
            Field maxClassMajorVersion = ASM.class.getDeclaredField("maxClassMajorVersion");
            Field maxClassMinorVersion = ASM.class.getDeclaredField("maxClassMinorVersion");
            Field maxJavaVersion = ASM.class.getDeclaredField("maxJavaVersion");
            Field API_VERSION = ASM.class.getDeclaredField("API_VERSION");

            majorVersion.setAccessible(true);
            minorVersion.setAccessible(true);
            implMinorVersion.setAccessible(true);
            patchVersion.setAccessible(true);
            maxVersion.setAccessible(true);
            maxClassVersion.setAccessible(true);
            maxClassMajorVersion.setAccessible(true);
            maxClassMinorVersion.setAccessible(true);
            maxJavaVersion.setAccessible(true);
            API_VERSION.setAccessible(true);

            majorVersion.set(null, shareMixinPatcher.majorVersion);
            minorVersion.set(null, shareMixinPatcher.minorVersion);
            implMinorVersion.set(null, shareMixinPatcher.implMinorVersion);
            patchVersion.set(null, shareMixinPatcher.patchVersion);
            maxVersion.set(null, shareMixinPatcher.maxVersion);
            maxClassVersion.set(null, shareMixinPatcher.maxClassVersion);
            maxClassMajorVersion.set(null, shareMixinPatcher.maxClassMajorVersion);
            maxClassMinorVersion.set(null, shareMixinPatcher.maxClassMinorVersion);
            maxJavaVersion.set(null, shareMixinPatcher.maxJavaVersion);
            API_VERSION.set(null, shareMixinPatcher.API_VERSION);

        } catch(Exception e) {
            Log.err(e);
        }
    }
}
