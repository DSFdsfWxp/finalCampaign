package finalCampaign.feature.featureBar.shortcut;

import arc.*;
import finalCampaign.event.*;
import finalCampaign.feature.featureBar.*;
import mindustry.*;
import mindustry.gen.*;

public class commandMode {

    public static fFeatureBar.togglableFeatureButton button;

    public static void register() {
        button = new fFeatureBar.togglableFeatureButton(Icon.units, "commandMode", () -> {
            Vars.control.input.commandMode = !Vars.control.input.commandMode;
            return false;
        });

        Events.on(fcInputHandleUpdateEvent.class, event -> {
            if (!event.beforeUpdate)
                button.setChecked(Vars.control.input.commandMode);
        });

        fFeatureBar.registerFetureButton(button);
    }
}
