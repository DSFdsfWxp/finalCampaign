package finalCampaign.feature.featureClass.control.freeVision;

import arc.Core;
import arc.scene.actions.*;
import arc.scene.ui.Label;
import arc.scene.ui.layout.*;
import arc.util.*;
import finalCampaign.*;
import mindustry.ui.*;

public class infoFragment extends Table {
    private float labelHeight;
    private boolean removed = false;

    public infoFragment() {
        setFillParent(false);
        table((t) -> {
            t.setFillParent(false);
            t.defaults().left();
            Label l = t.label(() -> {
                return fFreeVision.isOn() ? bundle.get("freeVision.onInfo") : bundle.get("freeVision.offInfo");
            }).style(Styles.outlineLabel).get();
            labelHeight = l.getHeight();
            t.setBackground(Styles.black6);
        }).margin(10f).center().bottom();
        color.a = 0f;
    }

    public void added() {
        setWidth(Core.scene.getWidth());
        setPosition(parent.x, parent.y + 20f);
        setHeight(labelHeight + 20f);
        actions(Actions.fadeIn(20f));
        Time.run(200f, () -> {
            actions(Actions.fadeOut(20f));
            Time.run(40f, () -> {
                if (removed) return;
                remove();
                removed = true;
            });
        });
    }

    @Override
    public boolean remove(){
        if (removed) return false;
        return super.remove();
    }

    @Override
    public void draw() {
        act(Time.delta);
        super.draw();
    }
}
