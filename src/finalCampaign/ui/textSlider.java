package finalCampaign.ui;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.struct.*;
import mindustry.graphics.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;

public class textSlider extends Table {
    protected Slider slider;
    protected Label label;
    protected Table flow;
    protected boolean ignoreChange = false;
    protected Floatf<Float> transformer = f -> f;
    protected Seq<line> lines;
    protected float maxRawValue, minRawValue, rawValueStep;
    
    public boolean showNum;
    public String numUnit;
    public boolean changeImmediately;

    public textSlider(String title, float value, float min, float max, float step, float width) {
        slider = new Slider(min, max, step, false);
        flow = new Table() {
            @Override
            public void draw() {
                for (line l : lines) l.draw();
                super.draw();
            }
        };
        lines = new Seq<>();

        setWidth(Scl.scl(width));
        slider.setWidth(Scl.scl(width));
        flow.setWidth(Scl.scl(width));
        maxRawValue = max;
        minRawValue = min;
        rawValueStep = step;
        showNum = true;
        changeImmediately = true;
        numUnit = "";

        flow.add(title).width(width * 0.6f).wrap().growY().left();
        label = flow.add(Float.toString(value)).width(width * 0.3f).wrap().growY().right().visible(() -> showNum).get();

        slider.changed(() -> {
            if (changeImmediately) this.change();

            float dv = transformer.get(slider.getValue());
            String placeholder = "";
            if (dv == Float.POSITIVE_INFINITY) placeholder = "∞";
            if (dv == Float.NEGATIVE_INFINITY) placeholder = "-∞";

            label.setText(placeholder.length() > 0 ? placeholder + numUnit : Float.toString(dv) + numUnit);
        });

        if (!changeImmediately) slider.addListener(new InputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                if (button == KeyCode.mouseLeft) textSlider.this.change();
            }
        });

        stack(slider, flow).width(width);
    }

    @Override
    public void change() {
        if (slider.isDisabled()) return;
        if (!ignoreChange) super.change();
    }

    public void transformer(Floatf<Float> trs) {
        transformer = trs;
    }

    public float value() {
        return transformer.get(slider.getValue());
    }

    public float rawValue() {
        return slider.getValue();
    }

    public void line(float rawValue) {
        lines.add(new line(rawValue));
    }

    public void rawValue(float v) {
        slider.setValue(v);
        ignoreChange = true;
        slider.change();
        ignoreChange = false;
    }

    public void setDisabled(boolean disabled) {
        slider.setDisabled(disabled);
    }

    protected class line {
        public float v;
        
        protected line(float v) {
            this.v = v;
        }

        public void draw() {
            float x = textSlider.this.x + textSlider.this.width * (v / textSlider.this.maxRawValue);
            Draw.color(Pal.lightishGray);
            Lines.stroke(Scl.scl(4f));
            Lines.line(x, textSlider.this.y, x, textSlider.this.y + textSlider.this.height);
            Draw.reset();
        }
    }
}
