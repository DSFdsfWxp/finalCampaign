package finalCampaign.map;

import arc.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.core.GameState.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.io.*;

public class fcMap {
    public static int currentVersion;
    public static final int version = 2;
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

    public static boolean sandbox() {
        return (Vars.state.rules.mode() == Gamemode.sandbox && initialMode == null) || initialMode == Gamemode.sandbox;
    }
}
