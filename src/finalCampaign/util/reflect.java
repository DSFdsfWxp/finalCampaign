package finalCampaign.util;

import java.lang.reflect.*;

public class reflect {
    public static <T> Constructor<T> getDeclaredConstructor(Class<T> c, Class<?> ...args) {
        try {
            return c.getDeclaredConstructor(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setAccessible(AccessibleObject object, boolean accessible) {
        try {
            object.setAccessible(accessible);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T newInstance(Constructor<T> constructor, Object ...args) {
        try {
            return constructor.newInstance(args);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
