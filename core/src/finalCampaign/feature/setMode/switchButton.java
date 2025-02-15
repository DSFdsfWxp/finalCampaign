package finalCampaign.feature.setMode;

import arc.scene.ui.*;
import arc.util.*;
import finalCampaign.*;
import finalCampaign.feature.hudUI.*;
import mindustry.gen.*;
import mindustry.ui.*;

public class switchButton {

    public static void setup() {
        fHudUI.freeLayer.fill(t -> {
            t.name = "fcSetModeSwitchButtonLayer";
            t.bottom().left();
            t.visible(fSetMode::isOn);

            TextButton.TextButtonStyle style = Styles.squareTogglet;
            try {
                style = Reflect.get(Styles.class, "clearTogglet");
            } catch (Throwable ignored) {}

            t.button(bundle.get("setMode.title"), Icon.settings, style, fSetMode::toggle)
                    .width(155f).height(48f).margin(12f).checked(b -> fSetMode.isOn()).name("fcSetModeSwitchButton");
        });

        fHudUI.fixedLayer.appendVisibility(fHudUI.fixedLayer.bottomLeft, () -> !fSetMode.isOn());
    }
}
