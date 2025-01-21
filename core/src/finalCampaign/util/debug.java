package finalCampaign.util;

import arc.util.*;

public class debug {
    public static void printStackTrace() {
        StackTraceElement[] lst = Thread.currentThread().getStackTrace();
        Log.debug("Stact Trace: ");
        for (int i=2; i<lst.length; i++) {
            Log.debug("  at @", lst[i].toString());
        }
    }
}
