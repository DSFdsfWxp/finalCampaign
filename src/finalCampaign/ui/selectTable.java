package finalCampaign.ui;

import arc.Core;
import arc.func.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.ui.*;

public class selectTable extends Table {
    public selectTable(Button b, Cons2<Table, Runnable> hideCons) {
        setBackground(Tex.paneSolid);
        margin(4f);

        Element hitter = new Element();

        Runnable hide = () -> {
            Core.app.post(hitter::remove);
            actions(Actions.fadeOut(0.3f, Interp.fade), Actions.remove());
        };

        hitter.fillParent = true;
        hitter.tapped(hide);

        Core.scene.add(hitter);
        Core.scene.add(this);

        update(() -> {
            if(b.parent == null || !b.isDescendantOf(Core.scene.root)){
                Core.app.post(() -> {
                    hitter.remove();
                    remove();
                });
                return;
            }

            b.localToStageCoordinates(Tmp.v1.set(b.getWidth()/2f, b.getHeight()/2f));
            setPosition(Tmp.v1.x, Tmp.v1.y, Align.center);
            if(getWidth() > Core.scene.getWidth()) setWidth(Core.graphics.getWidth());
            if(getHeight() > Core.scene.getHeight()) setHeight(Core.graphics.getHeight());
            keepInStage();
            invalidateHierarchy();
            pack();
        });
        actions(Actions.alpha(0), Actions.fadeIn(0.3f, Interp.fade));

        top().pane(inner -> {
            inner.top();
            hideCons.get(inner, hide);
        }).pad(0f).top().scrollX(false);

        pack();
    }

    @Override
    public float getPrefHeight(){
        return Math.min(super.getPrefHeight(), Core.graphics.getHeight());
    }

    @Override
    public float getPrefWidth(){
        return Math.min(super.getPrefWidth(), Core.graphics.getWidth());
    }

    public static <T> void showSelect(Button b, T[] values, T current, Cons<T> getter, int cols, Cons<Cell<?>> sizer){
        new selectTable(b, (t, hide) -> {
            ButtonGroup<Button> group = new ButtonGroup<>();
            group.setMinCheckCount(0);
            int i = 0;
            t.defaults().size(60f, 38f);

            for(T p : values) {
                boolean bundle = p.toString().startsWith("@");
                sizer.get(t.button(bundle ? finalCampaign.bundle.get(p.toString()) : p.toString(), Styles.logicTogglet, () -> {
                    getter.get(p);
                    hide.run();
                }).checked(current.equals(p)).group(group));

                if(++i % cols == 0) t.row();
            }
        });
    }

    public static <T> void showSelect(Button b, T[] values, T current, Cons<T> getter){
        showSelect(b, values, current, getter, 4, c -> {});
    }
}
