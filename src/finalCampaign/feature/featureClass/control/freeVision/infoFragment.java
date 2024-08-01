package finalCampaign.feature.featureClass.control.freeVision;

import arc.*;
import arc.scene.ui.*;
import arc.util.*;
import finalCampaign.*;
import finalCampaign.ui.layout.*;
import mindustry.ui.*;

public class infoFragment extends fragment {
    private float labelHeight;

    public infoFragment() {
        this.name = "fcFreeVisionInfo";
        table((t) -> {
            t.setFillParent(false);
            t.defaults().left();
            Label l = t.label(() -> {
                return fFreeVision.isOn() ? bundle.get("freeVision.onInfo") : bundle.get("freeVision.offInfo");
            }).style(Styles.outlineLabel).get();
            labelHeight = l.getHeight();
            t.setBackground(Styles.black6);
        }).margin(10f).center().bottom();
    }

    @Override
    public void added() {
        setWidth(Core.scene.getWidth());
        setPosition(parent.x, parent.y + 20f);
        setHeight(labelHeight + 20f);
        added(0.4f, () -> {
            Time.run(120f, () -> {
                remove(0.4f);
            });
        });
    }
}
