package finalCampaign.ui;

import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.actions.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.Label.*;
import arc.util.*;
import finalCampaign.ui.action.*;
import mindustry.core.*;
import mindustry.ui.*;

public class detailBar extends freeBar {
    private static Color dark = new Color(0.75f, 0.75f, 0.75f);
    private static LabelStyle style = new LabelStyle(Fonts.outline, Color.white);

    private boolean colorOverlay;
    private boolean actionRunning;
    private Color overlayColorFrom;
    private Color overlayColorDelta;

    public detailBar(float maxValue, Floatp value, Drawable icon, String title, Color color) {
        super(() -> Float.isNaN(maxValue) ? (value.get() != 0f ? 1f : 0f) : (value.get() / maxValue));
        colorOverlay = actionRunning = false;
        this.color.set(color);

        left();

        float fontHeight = add(title).style(style).padRight(2f).padLeft(16f).prefHeight();

        //outlineImage img = new outlineImage(icon, fontHeight * 0.95f, fontHeight * 0.95f, Scaling.fit);
        Image img = new Image(icon);
        add(img).padRight(2f).scaling(Scaling.fit).size(fontHeight * 0.95f);

        add(new Label(() -> {
            float v = value.get();
            if (v == Float.POSITIVE_INFINITY) return "∞";
            if (v == Float.NEGATIVE_INFINITY) return "-∞";
            return Float.toString(Mathf.floor(v * 100f) / 100f);
        })).style(style);

        if (!Float.isNaN(maxValue)) table(t -> {
            t.add("/" + UI.formatAmount((long) maxValue)).style(style).fontScale(0.8f).color(dark).padRight(8f).padTop(fontHeight * 0.2f);
            t.add(new Label(() -> (int) (value.get() / maxValue * 100) + "%")).style(style).fontScale(0.8f).color(dark).padTop(fontHeight * 0.2f);
        }).update(l -> {
            if (!actionRunning) {
                if (l.color.a == 0 && value.get() != maxValue) {
                    actionRunning = true;
                    Time.run(60f, () -> l.actions(Actions.alpha(1f, 0.5f, Interp.smooth), new runnableAction(() -> {
                        actionRunning = false;
                    })));
                }
                
                if (l.color.a > 0 && value.get() == maxValue) {
                    actionRunning = true;
                    Time.run(60f, () -> l.actions(Actions.alpha(0f, 0.5f, Interp.smooth), new runnableAction(() -> {
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
