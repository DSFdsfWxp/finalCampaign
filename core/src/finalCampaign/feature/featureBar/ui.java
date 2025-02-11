package finalCampaign.feature.featureBar;

import arc.scene.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;

public class ui {
    private static Table bar;

    private static void initBar(Table parent) {
        bar = new Table(Tex.pane);
        parent.add(bar).pad(4f).marginLeft(-4f).marginBottom(-4f).row();

    }
}
