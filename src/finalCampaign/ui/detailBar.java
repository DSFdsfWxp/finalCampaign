package finalCampaign.ui;

import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.actions.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.util.*;
import finalCampaign.ui.action.*;
import mindustry.core.UI;

public class detailBar extends freeBar {
    private static Color dark = new Color(0.75f, 0.75f, 0.75f);

    private boolean colorOverlay;
    private boolean actionRunning;
    private Color overlayColorFrom;
    private Color overlayColorDelta;

    public detailBar(float maxValue, Floatp value, Drawable icon, String title, Color color) {
        super(() -> value.get() / maxValue);
        colorOverlay = actionRunning = false;
        this.color.set(color);

        left();

        float fontHeight = add(title).padRight(4f).padLeft(16f).prefHeight();

        Image img = new Image(icon);
        add(img).scaling(Scaling.fit).size(fontHeight * 0.6f).padRight(4f);

        add(new Label(() -> {
            float v = value.get();
            if (v == Float.POSITIVE_INFINITY) return "∞";
            if (v == Float.NEGATIVE_INFINITY) return "-∞";
            return Long.toString((long)v);
        }));

        table(t -> {
            t.add("/" + UI.formatAmount((long) maxValue)).fontScale(0.8f).color(dark).padRight(8f).padTop(fontHeight * 0.2f);
            t.add(new Label(() -> Integer.toString((int)(value.get() / maxValue * 100)) + "%")).fontScale(0.8f).color(dark).padTop(fontHeight * 0.2f);
        }).update(l -> {
            if (!actionRunning) {
                if (l.color.a == 0 && value.get() != maxValue) {
                    actionRunning = true;
                    Time.run(60f, () -> l.actions(Actions.fadeIn(0.5f), new runnableAction(() -> {
                        actionRunning = false;
                    })));
                }
                
                if (l.color.a > 0 && value.get() == maxValue) {
                    actionRunning = true;
                    Time.run(60f, () -> l.actions(Actions.fadeOut(0.5f), new runnableAction(() -> {
                        actionRunning = false;
                    })));
                }
            }
        });

        update(() -> {
            if (!colorOverlay) return;
            float v = Mathf.clamp(Mathf.floor(value.get() / maxValue), 0f, 4f);

            if (v == 0) {
                color.set(overlayColorFrom);
                backgroundColor.set(0.1f, 0.1f, 0.1f);
            } else {
                backgroundColor.set(overlayColorDelta).mul(v).add(overlayColorFrom);
                color.set(overlayColorDelta).mul(v + 1).add(overlayColorFrom);
            }
        });
    }

    public detailBar colorOverlay(Color from, Color to) {
        colorOverlay = true;
        overlayColorFrom = from;
        overlayColorDelta = to.cpy().sub(from).mul(0.2f);
        return this;
    }
}
