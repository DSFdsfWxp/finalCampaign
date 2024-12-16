package finalCampaign.launch;

import java.io.*;
import arc.util.*;
import arc.util.Log.*;

public class shareLogger {
    private static PrintStream logFileStream;

    public static void setup() {
        String[] stags = {"&lc&fb[D]", "&lb&fb[I]", "&ly&fb[W]", "&lr&fb[E]", ""};
        String[] stagsNormal = {"[D]", "[I]", "[W]", "[E]", ""};

        Log.level = LogLevel.debug;

        if (shareMixinService.log) {
            try {
                bothFi logFi = bothFiles.instance.dataDirectory().child("fc_mod_launcher_log.txt");
                if (!logFi.exists()) logFi.writeString("init");
                logFileStream = new PrintStream(logFi.write());
            } catch(Throwable ignore) {}
        }

        Log.logger = (level, text) -> {
            System.out.println(Log.format(stags[level.ordinal()] + "&fr " + text));
            if (logFileStream != null) logFileStream.println(Log.format(stagsNormal[level.ordinal()] + " " + text));
        };
    }
}
