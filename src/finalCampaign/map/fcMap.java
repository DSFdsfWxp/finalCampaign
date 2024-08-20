package finalCampaign.map;

import arc.*;
import arc.util.*;
import mindustry.core.GameState.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.io.*;

public class fcMap {
    public static int currentVersion;
    public static final int version = 1;
    public static @Nullable Gamemode initialMode;

    public static void init() {
        Events.on(StateChangeEvent.class, event -> {
            if (event.to == State.menu) {
                reset();
            }
        });
        SaveVersion.addCustomChunk("finalCampaign.initialMode", new initialMode());
    }

    public static void reset() {
        currentVersion = 0;
        initialMode = null;
    }
}
