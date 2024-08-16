package finalCampaign.util;

public class arrays {
    @SafeVarargs
    public static <T> T[] of(T ...e) {
        return e;
    }

    public static <T> float[] ofF(float ...e) {
        return e;
    }

    public static <T> int[] ofI(int ...e) {
        return e;
    }
}
