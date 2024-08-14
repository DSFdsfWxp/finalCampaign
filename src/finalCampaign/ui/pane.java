package finalCampaign.ui;

import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.gen.*;

public abstract class pane extends Table {
    protected Table inner;
    private boolean selected, hovering;
    private Seq<Runnable> selectedChangedListeners;
    private boolean alwaysDrawBorder;

    public pane() {
        setBackground(Tex.sliderBack);
        inner = table().pad(4f).growX().get();
        selected = hovering = false;
        selectedChangedListeners = new Seq<>();
        alwaysDrawBorder = true;
    }

    public void alwaysDrawBorder(boolean v) {
        alwaysDrawBorder = v;
        if (!selected && !hovering) setBackground(v ? Tex.sliderBack : null);
    }

    public void setSelected(boolean v) {
        if (selected == v) return;
        selected = v;
        setBackground(selected ? Tex.buttonSelect : (alwaysDrawBorder ? Tex.sliderBack : null));
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
            if (!selected) setBackground(Tex.buttonSelect);
            hovering = true;
        });
        exited(() -> {
            if (!selected) setBackground(alwaysDrawBorder ? Tex.sliderBack : null);
            hovering = false;
        });
    }
}
