package finalCampaign.ui;

import arc.input.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.util.*;

public class dragHandle extends InputListener {
    float lastx, lasty;
    Element elem;
    dragLayout layout;
    
    public dragHandle(Element e, dragLayout layout) {
        elem = e;
        this.layout = layout;
    }
    
    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
        Vec2 v = elem.localToParentCoordinates(Tmp.v1.set(x, y));
        lastx = v.x;
        lasty = v.y;
        layout.drag(elem);
        elem.toFront();
        layout.layout();
        return true;
    }

    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer){
        Vec2 v = elem.localToParentCoordinates(Tmp.v1.set(x, y));

        elem.translation.add(v.x - lastx, v.y - lasty);
        lastx = v.x;
        lasty = v.y;

        layout.layout();
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
        layout.drag();
    }
}