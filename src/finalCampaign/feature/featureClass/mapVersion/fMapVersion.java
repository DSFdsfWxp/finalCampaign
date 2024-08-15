package finalCampaign.feature.featureClass.mapVersion;

import arc.*;
import mindustry.core.GameState.*;
import mindustry.game.EventType.*;

public class fMapVersion {
    public static final int version = 1;
    protected static int currentVersion = 0;

    public static boolean supported() {
        return true;
    }

    public static void init() {}

    public static void load() {
        Events.on(StateChangeEvent.class, e -> {
            if (e.to == State.menu) {
                currentVersion = 0;
            }
        });
    }

    public static void currentVersion(int v) {
        currentVersion = v;
    }

    public static int currentVersion() {
        return currentVersion;
    }
}
