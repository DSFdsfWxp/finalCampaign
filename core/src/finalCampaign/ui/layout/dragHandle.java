package finalCampaign.ui.layout;

import arc.input.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.util.*;

public class dragHandle extends InputListener {
    float lastx, lasty;
    Element elem;
    dragLayout layout;
    int draggingPointer;
    
    public dragHandle(Element e, dragLayout layout) {
        elem = e;
        this.layout = layout;
        draggingPointer = -1;
    }
    
    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
        if (draggingPointer != -1) return false;
        if (layout.dragging()) return false;
        draggingPointer = pointer;
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
        if (pointer != draggingPointer) return;
        draggingPointer = -1;
        layout.drag();
    }
}