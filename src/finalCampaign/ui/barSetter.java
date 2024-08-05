package finalCampaign.ui;

import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import finalCampaign.ui.limitedTextSlider.*;

public class barSetter extends Table {
    private float value;
    private limitedTextSlider slider;
    private TextField numField, percentField;

    public barSetter(String name, float width, float max, float min, float v, boolean isInt, boolean infinitable, boolean zeroable, boolean numSettable, boolean percentSettable) {
        slider = new limitedTextSlider(name, v / max, 0f, infinitable ? 125f : 100f, 0.01f, width);
        slider.showNum = false;
        slider.changeImmediately = false;
        value = v;

        if (!zeroable) {
            slider.hardLimit(-1f, 1f, limitSide.right);
            slider.line(1f);
        }
        if (infinitable) {
            slider.softLimit(100f, 124.99f, limitSide.left);
            slider.line(100f);
            slider.transformer(f -> f == 112.5f ? value : (f == 125f ? Float.POSITIVE_INFINITY : f * max));
        }

        slider.changed(() -> change(isInt ? (int) slider.value() : slider.value()));

        add(slider).center().width(170f).pad(4f).row();

        numField = field(slider.value() == Float.POSITIVE_INFINITY ? "∞" : Float.toString(slider.value()), txt -> {
            if (txt == "∞") {
                slider.rawValue(112.5f);
                value = Float.POSITIVE_INFINITY;
            } else {
                value = Float.parseFloat(txt);
                if (isInt) value = (int) value;
                slider.rawValue(value / max);
            }
            change(isInt ? Integer.MAX_VALUE : value);
        }).width(45f).left().padLeft(4f).padBottom(4f).valid(txt -> txt == "∞" || ((isInt ? Strings.canParseInt(txt) : Strings.canParseFloat(txt)) && Float.parseFloat(txt) >= min && Float.parseFloat(txt) <= max)).visible(numSettable).get();

        add("%").right().padRight(4f).padBottom(4f).visible(percentSettable);
        percentField = field(slider.value() == Float.POSITIVE_INFINITY ? "∞" : Float.toString(slider.value() / max * 100f), txt -> {
            if (txt == "∞") {
                slider.rawValue(112.5f);
                value = Float.POSITIVE_INFINITY;
            } else {
                value = Float.parseFloat(txt) / 100f * max;
                if (isInt) value = (int) value;
                slider.rawValue(value / max);
            }
            change(isInt ? Integer.MAX_VALUE : value);
        }).width(45f).right().padBottom(4f).valid(txt -> txt == "∞" || ((isInt ? Strings.canParseInt(txt) : Strings.canParseFloat(txt)) && Float.parseFloat(txt) >= 0f && Float.parseFloat(txt) <= 100f)).visible(percentSettable).get();

        slider.changed(() -> {
            boolean infinity = slider.value() == Float.POSITIVE_INFINITY;
            value = isInt ? (infinity ? Integer.MAX_VALUE : (int) slider.value()) : slider.value();
            numField.setText(infinity ? "∞" : Float.toString(value));
            percentField.setText(infinity ? "∞" : Float.toString(value / max * 100f));
        });
    }

    private void change(float v) {
        value = v;
        super.change();
    }

    public float value() {
        return value;
    }

    public void setDisabled(boolean disabled) {
        slider.setDisabled(disabled);
        numField.setDisabled(disabled);
        percentField.setDisabled(disabled);
    }
}
