package finalCampaign.feature.hudUIInfo;

import arc.func.*;
import arc.scene.ui.layout.*;
import finalCampaign.feature.hudUI.*;
import mindustry.ui.*;

public class ui {

    public static void show(Cons<Table> cons) {
        fHudUI.fixedLayer.centerBottom.table(t -> {
            t.setBackground(Styles.black6);
            t.table(cons).pad(4f);
        }).marginBottom(4f).row();
    }
}
