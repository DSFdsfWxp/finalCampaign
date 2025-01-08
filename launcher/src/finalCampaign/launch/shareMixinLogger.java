package finalCampaign.launch;

import arc.util.*;
import arc.util.Log.*;
import arc.struct.*;
import org.spongepowered.asm.logging.*;

public class shareMixinLogger extends LoggerAdapterAbstract {
    public static boolean enableLog = false;

    private ObjectMap<Level, LogLevel> map;

    public shareMixinLogger(String name) {
        super(name);

        map = new ObjectMap<>();
        map.put(Level.INFO, LogLevel.info);
        map.put(Level.WARN, LogLevel.warn);
        map.put(Level.ERROR, LogLevel.err);
        map.put(Level.FATAL, LogLevel.err);
        map.put(Level.DEBUG, LogLevel.debug);
        map.put(Level.TRACE, LogLevel.info);
    }

    public String getType() {
        return "finalCampaign Mixin Logger";
    }

    public void log(Level level, String text, Object ...args) {
        if (!enableLog && (level != Level.ERROR && level != Level.FATAL)) return;
        Log.log(map.get(level), "[mixin] " + text.replace("{}", "@"), args);
    }
    public void log(Level level, String message, Throwable t) {
        log(level, message, new Object[]{t});
    }

    public void catching(Level level, Throwable t) {
        log(level, "Catching ".concat(t.toString()), t);
    }

    public <T extends Throwable> T throwing(T t) {
        log(Level.ERROR, "Throwing ".concat(t.toString()), (Throwable)t);
        return t;
    }
}
