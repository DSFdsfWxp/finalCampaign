package finalCampaign.feature.setMode.feature;

import arc.scene.ui.layout.*;
import finalCampaign.ui.*;
import mindustry.gen.*;

public abstract class bBarSetter extends bAttributeSetter {
    protected float max, min, v;
    protected boolean Int, infinitable, zeroable, numSettable, percentSettable;

    public bBarSetter(String name, boolean sandboxOnly) {
        super(name, "set", sandboxOnly);
    }

    public bBarSetter(String name, boolean sandboxOnly, float max, float min, float v, boolean isInt, boolean infinitable, boolean zeroable, boolean numSettable, boolean percentSettable) {
        super(name, "set", sandboxOnly);

        this.max = max;
        this.min = min;
        this.v = v;
        Int = isInt;
        this.infinitable = infinitable;
        this.zeroable = zeroable;
        this.numSettable = numSettable;
        this.percentSettable = percentSettable;
    }

    public void buildUI(Building[] building, Table table) {
        barSetter setter = new barSetter("", 256f, max, min, v, Int, infinitable, zeroable, numSettable, percentSettable);
        setter.modified(() -> change(building, setter.value()));
        table.add(setter).growX();
    }

    public abstract void change(Building[] selected, float value);
}
