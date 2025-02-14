package finalCampaign.feature.hudUI;

import arc.func.*;
import arc.scene.actions.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import finalCampaign.feature.featureBar.*;
import finalCampaign.ui.action.*;
import mindustry.*;
import mindustry.ui.*;

public class fHudUI {

    public static hudFixedLayer fixedLayer;
    public static hudWindowLayer windowLayer;


    public static boolean supported() {
        return !Vars.headless;
    }

    public static void earlyInit() {
        fixedLayer = new hudFixedLayer();
        windowLayer = new hudWindowLayer();
    }

    public static void earlyLoad() {
        fixedLayer.init();
    }

    public static void lateLoad() {
        fixedLayer.setup(Vars.ui.hudGroup);
        windowLayer.setup(Vars.ui.hudGroup);

        rebuildFixedLayer();
    }

    public static void showBottomToast(Cons2<Table, Runnable> handle) {
        showAtCenterBottom(t -> {
            Runnable close = () -> {
                t.clearActions();
                t.actions(
                        Actions.sequence(
                                Actions.parallel(
                                        Actions.fadeOut(0.4f),
                                        Actions.scaleTo(0f, 0f, 0.4f)
                                ),
                                Actions.remove()
                        )
                );
            };

            handle.get(t, close);
            t.setTransform(true);
            t.setOrigin(Align.center);
            t.actions(
                    Actions.fadeIn(0.4f)
            );
        });
    }

    public static void showBottomToast(Cons<Table> builder) {
        showBottomToast((t, close) -> {
            builder.get(t);
            t.actions(
                    Actions.sequence(
                            Actions.delay(2.4f),
                            Actions.run(close)
                    )
            );
        });
    }

    public static void showBottomToast(String txt) {
        showBottomToast(t -> t.add(txt));
    }

    public static void showAtCenterBottom(Cons<Table> builder) {
        fixedLayer.centerBottom.table(t -> {
            t.setBackground(Styles.black6);
            t.table(builder).pad(4f);
        }).marginBottom(4f).row();
    }

    public static void rebuildFixedLayer() {
        // bottom left
        {
            fixedLayer.bottomLeft.clear();

            fFeatureBar.buildUI();
            fixedLayer.bottomLeft.row();
        }
    }
}
