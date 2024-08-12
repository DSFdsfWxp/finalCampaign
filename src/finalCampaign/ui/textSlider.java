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
    protected boolean ignoreModify = false;
    protected Floatf<Float> transformer = f -> f;
    protected Seq<line> lines;
    protected Seq<Runnable> modifiedListener;
    protected float maxRawValue, minRawValue, rawValueStep;
    
    public boolean showNum;
    public String numUnit;
    public boolean modifyImmediately;

    public textSlider(String title, float value, float min, float max, float step, float width) {
        slider = new Slider(min, max, step, false);
        flow = new Table() {
            @Override
            public void draw() {
                applyTransform(computeTransform());
                for (line l : lines) l.draw();
                resetTransform();
                super.draw();
            }
        };
        lines = new Seq<>();
        modifiedListener = new Seq<>();

        setWidth(Scl.scl(width));
        slider.setWidth(Scl.scl(width));
        slider.setValue(value);
        slider.change();
        flow.setWidth(Scl.scl(width));
        flow.touchable = Touchable.disabled;
        maxRawValue = max;
        minRawValue = min;
        rawValueStep = step;
        showNum = true;
        modifyImmediately = true;
        numUnit = "";

        flow.left();
        flow.add(title).colspan(6).expandX().left();
        label = flow.add(Float.toString(value)).colspan(4).expandX().right().visible(() -> showNum).get();

        slider.changed(() -> {
            if (modifyImmediately) modify();

            float dv = transformer.get(slider.getValue());
            String placeholder = "";
            if (dv == Float.POSITIVE_INFINITY) placeholder = "∞";
            if (dv == Float.NEGATIVE_INFINITY) placeholder = "-∞";

            if (showNum) label.setText(placeholder.length() > 0 ? placeholder + numUnit : Float.toString(dv) + numUnit);
        });

        slider.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                return true;
            }
            
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                if (modifyImmediately) return;
                textSlider.this.modify();
            }
        });

        stack(slider, flow).width(width);
    }

    public void modify() {
        if (slider.isDisabled()) return;
        if (!ignoreModify) for (Runnable run : modifiedListener) run.run();
    }

    public void modified(Runnable run) {
        if (!modifiedListener.contains(run)) modifiedListener.add(run);
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
        ignoreModify = true;
        slider.setValue(v);
        slider.change();
        ignoreModify = false;
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
            float x = textSlider.this.width * (v / textSlider.this.maxRawValue);
            Draw.color(Pal.lightishGray);
            Lines.stroke(4f);
            Lines.line(x, 6f, x, textSlider.this.height - 6f);
            Draw.reset();
        }
    }
}
