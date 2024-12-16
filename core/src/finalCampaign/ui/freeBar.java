package finalCampaign.ui;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;

public class freeBar extends Table {
    private static Rect scissor = new Rect();

    public Color backgroundColor = new Color(0.1f, 0.1f, 0.1f);
    public Color blinkColor = new Color();
    public Color outlineColor = new Color();

    public Floatp fraction;
    private float value;
    private float lastValue;
    private float blink;
    private float outlineRadius;

    public freeBar(Floatp v) {
        fraction = v;

        setBackground(new BaseDrawable() {
            @Override
            public void draw(float x, float y, float width, float height){
                if(fraction == null) return;

                float computed = Mathf.clamp(fraction.get());
                
                if(lastValue > computed) blink = 1f;
                lastValue = computed;
        
                if(Float.isNaN(lastValue)) lastValue = 0;
                if(Float.isInfinite(lastValue)) lastValue = 1f;
                if(Float.isNaN(value)) value = 0;
                if(Float.isInfinite(value)) value = 1f;
                if(Float.isNaN(computed)) computed = 0;
                if(Float.isInfinite(computed)) computed = 1f;
        
                blink = Mathf.lerpDelta(blink, 0f, 0.2f);
                value = Mathf.lerpDelta(value, computed, 0.15f);
        
                Drawable bar = Tex.bar;
        
                if(outlineRadius > 0){
                    Draw.color(outlineColor);
                    bar.draw(x - outlineRadius, y - outlineRadius, width + outlineRadius*2, height + outlineRadius*2);
                }
        
                Draw.color(backgroundColor);
                Draw.alpha(parentAlpha);
                bar.draw(x, y, width, height);
                Draw.color(color, blinkColor, blink);
                Draw.alpha(parentAlpha);
        
                Drawable top = Tex.barTop;
                float topWidth = width * value;
        
                if(topWidth > Core.atlas.find("bar-top").width){
                    top.draw(x, y, topWidth, height);
                }else{
                    if(ScissorStack.push(scissor.set(x, y, topWidth, height))){
                        top.draw(x, y, Core.atlas.find("bar-top").width, height);
                        ScissorStack.pop();
                    }
                }
        
                Draw.color();
            }
        });

        snap();
    }

    public void snap() {
        this.lastValue = this.value = this.fraction.get();
    }

    public freeBar fraction(Floatp v) {
        fraction = v;
        return this;
    }

    public freeBar backgroundColor(Color v) {
        this.backgroundColor = v;
        return this;
    }

    public freeBar blink(Color v) {
        this.blinkColor = v;
        return this;
    }

    public freeBar outline(Color v, float stroke) {
        this.outlineColor = v;
        outlineRadius = stroke;
        return this;
    }
}
