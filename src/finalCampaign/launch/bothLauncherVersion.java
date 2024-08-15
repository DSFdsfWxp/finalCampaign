package finalCampaign.launch;

public class bothLauncherVersion {
    public final static int majorD = 0;
    public final static int minorD = 0;
    public final static int debugD = 15;

    public final static int majorA = 0;
    public final static int minorA = 0;
    public final static int debugA = 1;

    public static String toDesktopVersionString() {
        return String.format("%d.%d.%d", majorD, minorD, debugD);
    }

    public static String toAndoridVersionString() {
        return String.format("%d.%d.%d", majorA, minorA, debugA);
    }
}
