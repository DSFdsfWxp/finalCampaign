package finalCampaign.ui;

import arc.graphics.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.gen.*;
import mindustry.graphics.*;

public abstract class pane extends Table {
    protected Table inner;
    private boolean selected;
    private Seq<Runnable> selectedChangedListeners;

    public pane() {
        setBackground(Tex.sliderBack);
        inner = table().pad(4f).growX().get();
        selected = false;
        selectedChangedListeners = new Seq<>();
    }

    public void setSelected(boolean v) {
        if (selected == v) return;
        selected = v;
        setColor(selected ? Pal.accent : Color.white);
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
            if (!selected) setColor(Pal.accent.cpy().a(0.7f));
        });
        exited(() -> {
            if (!selected) setColor(Color.white);
        });
    }
}
