package finalCampaign.feature.featureClass.control.setMode;

import arc.struct.*;
import arc.Events;
import arc.func.*;
import arc.scene.*;
import mindustry.*;
import mindustry.core.GameState.*;
import mindustry.game.EventType.*;

public class uiPatcher {
    private static Seq<String> whiteList = new Seq<>(new String[] {
        "paused",
        "waiting",
        "overlaymarker",
        "coreinfo",
        "nearpoint",
        "saving",
        "fcPlacementFragment",
        "fcSetModeFragment"
    });

    private static Seq<String> patched = new Seq<>();

    public static void load() {
        Events.on(StateChangeEvent.class, e -> {
            if (e.to == State.playing) {
                patch();
            }
        });
    }

    public static void patch() {
        for (Element e : Vars.ui.hudGroup.getChildren()) {
            if (!whiteList.contains(e.name) && !patched.contains(e.getClass().getName() + " @ " + Integer.toHexString(System.identityHashCode(e)))) {
                Boolp original = e.visibility;
                e.visibility = () -> original.get() && !fSetMode.isOn();
                patched.add(e.getClass().getName() + " @ " + Integer.toHexString(System.identityHashCode(e)));
            }
        }
    }
}
