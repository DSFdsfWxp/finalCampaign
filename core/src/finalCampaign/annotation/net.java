package finalCampaign.annotation;

import java.lang.annotation.*;

public class net {

    public static enum packetSource {
        client,
        server,
        both
    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface netCall {
        public packetSource src();
        public boolean reliable() default true;

        public boolean nullCheck() default true;
        public boolean deadCheck() default true;
        public boolean teamCheck() default true;
        public boolean buildingModuleCheck() default true;
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface nullCheckExclude {
        public String[] value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface deadCheckExclude {
        public String[] value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface teamCheckOpt {
        public String[] exclude();
        public boolean skipInSandbox() default true;
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface buildingTarget {
        public Class<?>[] value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface sandboxOnly {}
}
