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

    public static void fillF(float[] arr, float v) {
        for (int i=0; i<arr.length; i++) arr[i] = v;
    }

    public static void fillD(double[] arr, double v) {
        for (int i=0; i<arr.length; i++) arr[i] = v;
    }

    public static float[] ensureLengthF(float[] arr, int length) {
        if (arr == null || arr.length < length) return new float[length];
        return arr;
    }
}
