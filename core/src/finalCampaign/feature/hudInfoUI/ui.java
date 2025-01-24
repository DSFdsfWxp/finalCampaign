package finalCampaign.feature.hudInfoUI;

import arc.func.*;
import arc.scene.ui.layout.*;
import mindustry.*;
import mindustry.ui.*;

public class ui {
    private static Table frag;

    public static void init() {
        Vars.ui.hudGroup.fill(t -> {
            frag = t;
            t.name = "fcHudInfoFrag";
            t.bottom();
            t.table().update(tt -> {
                tt.setHeight(logic.calcHintHeight());
            });
        });
    }

    public static void show(Cons<Table> cons) {
        frag.table(t -> {
            t.setBackground(Styles.black6);
            t.table(cons).pad(4f);
        }).marginBottom(4f);
    }
}
