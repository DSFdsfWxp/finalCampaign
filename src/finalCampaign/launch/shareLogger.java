package finalCampaign.launch;

import java.io.*;
import arc.*;
import arc.files.*;
import arc.util.*;
import arc.util.Log.*;

public class shareLogger {
    public static void setup() {
        String[] stags = {"&lc&fb[D]", "&lb&fb[I]", "&ly&fb[W]", "&lr&fb[E]", ""};

        Log.level = LogLevel.debug;

        Log.logger = (level, text) -> {
            String rawText = Log.format(stags[level.ordinal()] + "&fr " + text);
            System.out.println(rawText);
        };

        try{
            Fi logFile = Core.settings.getDataDirectory().child("last_log.txt");
            Fi outStreamFile = Core.settings.getDataDirectory().child("out_stream.txt");

            if (!logFile.exists()) logFile.writeString("init");
            if (!outStreamFile.exists() && OS.isAndroid) outStreamFile.writeString("init");
            
            Writer writer = logFile.writer(false);
            LogHandler log = Log.logger;
            Log.logger = (level, text) -> {
                log.log(level, text);

                try{
                    writer.write("[" + Character.toUpperCase(level.name().charAt(0)) + "] " + Log.removeColors(text) + "\n");
                    writer.flush();
                }catch(IOException e){
                    e.printStackTrace();
                    //ignore it
                }
            };

            if (OS.isAndroid) {
                PrintStream outStream = new PrintStream(outStreamFile.write());
                System.setOut(outStream);
                System.setErr(outStream);
            }

        }catch(Exception e){
            //handle log file not being found
            Log.err(e);
        }
    }
}
