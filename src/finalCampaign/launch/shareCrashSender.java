package finalCampaign.launch;

import java.lang.Thread.*;
import arc.*;
import arc.util.*;

public abstract class shareCrashSender {
    public abstract String createReport(String error);
    public static shareCrashSender sender;

    public void log(Throwable exception){
        try {
            Core.settings.getDataDirectory().child("crashes").child("crash_" + System.currentTimeMillis() + ".txt")
            .writeString(createReport(Strings.neatError(exception)));
        } catch(Throwable ignored) {}
    }

    public static void setDefaultUncaughtExceptionHandler(shareCrashSender sender) {
        shareCrashSender.sender = sender;
        UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            sender.log(error);

            //try to forward exception to system handler
            if(handler != null){
                handler.uncaughtException(thread, error);
            }else{
                Log.err(error);
                System.exit(1);
            }
        });
    }
}
