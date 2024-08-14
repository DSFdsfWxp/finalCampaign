package finalCampaign.ui;

import arc.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import finalCampaign.ui.limitedTextSlider.*;

public class barSetter extends Table {
    private float value;
    private limitedTextSlider slider;
    private TextField numField, percentField;
    private Seq<Runnable> modifiedListener;

    public barSetter(String name, float width, float max, float min, float v, boolean isInt, boolean infinitable, boolean zeroable, boolean numSettable, boolean percentSettable) {
        slider = new limitedTextSlider(name, v / max * 100f, 0f, infinitable ? 125f : 100f, 0.01f, width);
        modifiedListener = new Seq<>();
        slider.showNum = false;
        slider.modifyImmediately = false;
        value = v;

        if (!zeroable) {
            slider.hardLimit(-1f, 1f, limitSide.right);
            slider.line(1f);
        }
        if (infinitable) {
            slider.softLimit(100f, 124.99f, limitSide.left);
            slider.line(100f);
            slider.transformer(f -> f == 125f ? Float.POSITIVE_INFINITY : f / 100f * max);
        }

        slider.modified(() -> modify(isInt ? (int) slider.value() : slider.value()));

        add(slider).center().width(width).pad(4f).colspan(3).row();

        numField = field(slider.value() == Float.POSITIVE_INFINITY ? "∞" : (isInt ? Integer.toString((int) slider.value()) : Float.toString(slider.value())), txt -> {
            if (txt.equals("∞")) {
                slider.rawValue(125f);
                value = Float.POSITIVE_INFINITY;
            } else {
                value = Float.parseFloat(txt);
                if (isInt) value = (int) value;
                slider.rawValue(value / max * 100f);
                boolean infinity = slider.value() == Float.POSITIVE_INFINITY;
                percentField.setText(infinity ? "∞" : Float.toString(value / max * 100f));
            }
            modify(isInt ? (int) value : value);
        }).minWidth(50f).growX().left().padLeft(4f).padBottom(4f).valid(txt -> txt.equals("∞") || ((isInt ? Strings.canParseInt(txt) : Strings.canParseFloat(txt)) && Float.parseFloat(txt) >= min && Float.parseFloat(txt) <= max)).visible(numSettable).get();
        
        percentField = field(slider.value() == Float.POSITIVE_INFINITY ? "∞" : Float.toString(slider.value() / max * 100f), txt -> {
            if (txt.equals("∞")) {
                slider.rawValue(125f);
                value = Float.POSITIVE_INFINITY;
            } else {
                value = Float.parseFloat(txt) / 100f * max;
                if (isInt) value = (int) value;
                slider.rawValue(value / max * 100f);
                boolean infinity = slider.value() == Float.POSITIVE_INFINITY;
                numField.setText(infinity ? "∞" : (isInt ? Integer.toString((int) value) : Float.toString(value)));
            }
            modify(isInt ? (int) value : value);
        }).growX().minWidth(50f).padLeft(4f).expandX().right().padBottom(4f).valid(txt -> txt.equals("∞") || (Strings.canParseFloat(txt) && Float.parseFloat(txt) >= 0f && Float.parseFloat(txt) <= 100f)).visible(percentSettable).get();
        add("%").right().padRight(4f).padBottom(4f).visible(percentSettable);

        slider.immediatelyModified(() -> {
            if (Core.scene.hasField()) return;

            if (infinitable && slider.rawValue() > 100f && slider.rawValue() < 125f) {
                value = isInt ? (int) max : max;
                numField.setText(isInt ? Integer.toString((int) value) : Float.toString(value));
                percentField.setText("100.0");
            } else {
                boolean infinity = slider.value() == Float.POSITIVE_INFINITY;
                value = isInt ? (infinity ? Integer.MAX_VALUE : (int) slider.value()) : slider.value();
                numField.setText(infinity ? "∞" : (isInt ? Integer.toString((int) value) : Float.toString(value)));
                percentField.setText(infinity ? "∞" : Float.toString(value / max * 100f));
            }
        });
    }

    private void modify(float v) {
        value = v;
        modify();
    }

    public void modify() {
        for (Runnable run : modifiedListener) run.run();
    }

    public void modified(Runnable run) {
        if (!modifiedListener.contains(run)) modifiedListener.add(run);
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
