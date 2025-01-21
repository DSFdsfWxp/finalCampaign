package finalCampaign.ui;

import arc.*;
import arc.input.*;
import arc.scene.event.*;
import arc.struct.*;

public class limitedTextSlider extends textSlider {
    private Seq<limit> limits;
    
    public limitedTextSlider(String title, float value, float min, float max, float step, float width) {
        super(title, value, min, max, step, width);
        limits = new Seq<>();
        slider.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                for (limit l : limits) l.check();
            }
        });
    }

    public void hardLimit(float from, float to, limitSide side) {
        limits.add(new limit(from, to, true, side));
    }

    public void softLimit(float from, float to, limitSide side) {
        limits.add(new limit(from, to, false, side));
    }

    @Override
    public void immediatelyModify() {
        for (limit l : limits) l.check();
        super.immediatelyModify();
    }

    public enum limitSide {
        left,
        right
    }

    protected class limit {
        public float from;
        public float to;
        public boolean hard;
        public limitSide side;

        protected limit(float from, float to, boolean hard, limitSide side) {
            this.from = from;
            this.to = to;
            this.hard = hard;
            this.side = side;
        }

        public void check() {
            float current = limitedTextSlider.this.rawValue();
            if (current > from && current < to) {
                if (hard || !Core.input.keyDown(KeyCode.mouseLeft)) {
                    limitedTextSlider.this.rawValue(side == limitSide.left ? from : to);
                    limitedTextSlider.this.modify();
                }
                limitedTextSlider.this.ignoreModify = true;
            } else {
                limitedTextSlider.this.ignoreModify = false;
            }
        }
    }
}
