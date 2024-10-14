package finalCampaign.ui;

import arc.func.*;
import arc.graphics.*;
import arc.scene.event.*;
import mindustry.ui.*;

public class fakeBar extends Bar {
    private freeBar proxy;
    public Floatp fraction;

    public fakeBar(freeBar target) {
        proxy = target;
        fraction = target.fraction;
    }

    @Override
    public boolean fire(SceneEvent event) {
        boolean p = proxy.fire(event);
        boolean s = super.fire(event);
        return p && s;
    }

    @Override
    public void draw() {
        super.invalidate();
        proxy.setPosition(x, y);
        proxy.setSize(width, height);
        proxy.invalidate();
        proxy.fraction(fraction);
        proxy.draw();
    }

    @Override
    public void act(float v) {
        super.act(v);
        proxy.act(v);
    }

    @Override
    public Bar blink(Color color) {
        proxy.blink(color);
        return this;
    }

    @Override
    public Bar outline(Color color, float stroke) {
        proxy.outline(color, stroke);
        return this;
    }
}
