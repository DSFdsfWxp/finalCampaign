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

    public static <T> Field getDeclaredField(Class<T> c, String name) {
        try {
            return c.getDeclaredField(name);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Field field, Object object) {
        try {
            return (T) field.get(object);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
