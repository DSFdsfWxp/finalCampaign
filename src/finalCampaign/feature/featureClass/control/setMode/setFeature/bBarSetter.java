package finalCampaign.feature.featureClass.control.setMode.setFeature;

import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.Strings;
import finalCampaign.*;
import finalCampaign.ui.*;
import finalCampaign.ui.limitedTextSlider.*;
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
        limitedTextSlider slider = new limitedTextSlider(bundle.get("num"), v / max, 0f, infinitable ? 125f : 100f, 0.01f, 164f);
        slider.numUnit = "%";

        if (!zeroable) {
            slider.hardLimit(-1f, 1f, limitSide.right);
            slider.line(1f);
        }
        if (infinitable) {
            slider.softLimit(100f, 124.99f, limitSide.left);
            slider.line(100f);
            slider.transformer(f -> f == 112.5f ? v : (f == 125f ? Float.POSITIVE_INFINITY : f * max));
        }

        slider.changed(() -> change(building, Int ? (int) slider.value() : slider.value()));

        table.add(slider).center().width(170f).pad(4f).row();

        TextField numField = table.field(slider.value() == Float.POSITIVE_INFINITY ? "∞" : Float.toString(slider.value()), txt -> {
            if (txt == "∞") {
                slider.rawValue(112.5f);
                v = Float.POSITIVE_INFINITY;
            } else {
                v = Float.parseFloat(txt);
                if (Int) v = (int) v;
                slider.rawValue(v / max);
            }
            change(building, Int ? Integer.MAX_VALUE : v);
        }).width(45f).left().padLeft(4f).padBottom(4f).valid(txt -> txt == "∞" || ((Int ? Strings.canParseInt(txt) : Strings.canParseFloat(txt)) && Float.parseFloat(txt) >= min && Float.parseFloat(txt) <= max)).visible(numSettable).get();

        table.add("%").right().padRight(4f).padBottom(4f).visible(percentSettable);
        TextField percentNum = table.field(slider.value() == Float.POSITIVE_INFINITY ? "∞" : Float.toString(slider.value() / max * 100f), txt -> {
            if (txt == "∞") {
                slider.rawValue(112.5f);
                v = Float.POSITIVE_INFINITY;
            } else {
                v = Float.parseFloat(txt) / 100f * max;
                if (Int) v = (int) v;
                slider.rawValue(v / max);
            }
            change(building, Int ? Integer.MAX_VALUE : v);
        }).width(45f).right().padBottom(4f).valid(txt -> txt == "∞" || ((Int ? Strings.canParseInt(txt) : Strings.canParseFloat(txt)) && Float.parseFloat(txt) >= 0f && Float.parseFloat(txt) <= 100f)).visible(percentSettable).get();

        slider.changed(() -> {
            boolean infinity = slider.value() == Float.POSITIVE_INFINITY;
            v = Int ? (infinity ? Integer.MAX_VALUE : (int) slider.value()) : slider.value();
            numField.setText(infinity ? "∞" : Float.toString(v));
            percentNum.setText(infinity ? "∞" : Float.toString(v / max * 100f));
        });
    }

    public abstract void change(Building[] selected, float value);
}
