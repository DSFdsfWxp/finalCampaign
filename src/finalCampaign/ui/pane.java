package finalCampaign.ui;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.gen.*;
import mindustry.graphics.*;

public abstract class pane extends Table {
    protected Table inner;
    private boolean selected, hovering;
    private Seq<Runnable> selectedChangedListeners;
    private boolean alwaysDrawBorder;
    private float backgroundDarkness;

    public pane() {
        setBackground(Tex.whitePane);
        setColor(Color.darkGray);
        inner = table().growX().get();
        selected = hovering = false;
        selectedChangedListeners = new Seq<>();
        backgroundDarkness = 0f;
        alwaysDrawBorder = true;
    }

    @Override
    protected void drawBackground(float x, float y) {
        Draw.color(0, 0, 0, backgroundDarkness);
        Fill.crect(x, y, width, height);
        if (!alwaysDrawBorder && !selected && !hovering) return;
        super.drawBackground(x, y);
    }

    public void backgroundDarkness(float v) {
        backgroundDarkness = v;
    }

    public void alwaysDrawBorder(boolean v) {
        alwaysDrawBorder = v;
        if (!selected && !hovering) setColor(Color.darkGray);
    }

    public void setSelected(boolean v) {
        if (selected == v) return;
        selected = v;
        setColor(selected ? Pal.accent : Color.darkGray);
    }

    public void setSelected(boolean v, boolean fireEvent) {
        setSelected(v);
        if (fireEvent) fireSelectedChanged();
    }

    public boolean selected() {
        return selected;
    }

    public void selectedChanged(Runnable run) {
        selectedChangedListeners.add(run);
    }

    public void fireSelectedChanged() {
        for (Runnable r : selectedChangedListeners) r.run();
    }

    public boolean toggleSelected() {
        setSelected(!selected);
        fireSelectedChanged();
        return selected;
    }

    public void addHoveredListener() {
        hovered(() -> {
            if (!selected) {
                setColor(Color.gray);
            }
            hovering = true;
        });
        exited(() -> {
            if (!selected) setColor(Color.darkGray);
            hovering = false;
        });
    }
}
