package finalCampaign.ui;

import arc.scene.ui.*;
import arc.scene.ui.layout.*;

public class textSlider extends Table {
    private Slider slider;
    private boolean ignoreChange = false;

    public textSlider(String title, float value, float min, float max, float step, float width) {
        slider = new Slider(min, max, step, false);
        Table flow = new Table();

        setWidth(Scl.scl(width));
        slider.setWidth(Scl.scl(width));
        flow.setWidth(Scl.scl(width));

        flow.add(title).width(width * 0.6f).wrap().growY().left();
        Label label = flow.add(Float.toString(value)).width(width * 0.3f).wrap().growY().right().get();

        slider.changed(() -> {
            if (!ignoreChange) this.change();
            label.setText(Float.toString(slider.getValue()));
        });

        stack(slider, flow).width(width);
    }

    public float value() {
        return slider.getValue();
    }

    public void value(float v) {
        slider.setValue(v);
        ignoreChange = true;
        slider.change();
        ignoreChange = false;
    }
}
