package finalCampaign.feature.hudUIInfo;

import arc.*;
import arc.func.*;
import arc.scene.actions.*;
import arc.scene.ui.layout.*;
import finalCampaign.event.*;
import mindustry.*;

public class fHudUIInfo {
    private static boolean loaded = false;

    public static boolean supported() {
        return !Vars.headless;
    }

    public static void earlyLoad() {
        Events.on(fcInputHandleBuildUIEvent.class, logic::buildUI);

        loaded = true;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static void showHint(Cons<Table> cons) {
        ui.show(t -> {
            cons.get(t);
            t.actions(
                    Actions.sequence(
                            Actions.fadeIn(0.4f),
                            Actions.delay(2f),
                            Actions.parallel(
                                    Actions.fadeOut(0.4f),
                                    Actions.scaleTo(0f, 0f, 0.4f)
                            ),
                            Actions.remove()
                    )
            );
        });
    }

    public static void showTextHint(String txt) {
        showHint(t -> t.add(txt));
    }
}
