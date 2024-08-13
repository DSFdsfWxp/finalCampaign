package finalCampaign.ui;

import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.gen.*;

public class dragLayoutX extends dragLayout {
    float space, prefWidth, prefHeight, targetHeight;
    Seq<Element> seq = new Seq<>();
    int insertPosition = 0;
    boolean invalidated;

    public dragLayoutX(float space, float height) {
        setTransform(true);
        this.space = Scl.scl(space);
        targetHeight = height;
    }

    @Override
    public void layout(){
        invalidated = true;
        float cx = 0;
        seq.clear();

        float totalWidth = getChildren().sumf(e -> e.getWidth() + space);

        width = prefWidth = totalWidth;
        height = prefHeight = Scl.scl(targetHeight);

        //layout everything normally
        for(int i = 0; i < getChildren().size; i++){
            Element e = getChildren().get(i);

            //ignore the dragged element
            if(dragging == e) continue;

            e.setSize(e.getPrefWidth(), height);
            e.setPosition(cx, 0);

            cx += e.getPrefWidth() + space;
            seq.add(e);
        }

        //insert the dragged element if necessary
        if(dragging != null){
            //find real position of dragged element top
            float realX = dragging.x + dragging.translation.x;

            insertPosition = 0;

            for(int i = 0; i < seq.size; i++){
                Element cur = seq.get(i);
                //find fit point
                if(realX > cur.x && (i == seq.size - 1 || realX < seq.get(i + 1).x)) {
                    insertPosition = i + 1;
                    break;
                }
            }

            float shiftAmount = dragging.getWidth() + space;

            //shift elements below insertion point down
            for(int i = insertPosition; i < seq.size; i++){
                seq.get(i).x += shiftAmount;
            }
        }

        invalidateHierarchy();

        if(parent != null && parent instanceof Table){
            setCullingArea(parent.getCullingArea());
        }
    }

    @Override
    public float getPrefWidth(){
        return prefWidth;
    }

    @Override
    public float getPrefHeight(){
        return prefHeight;
    }

    @Override
    public void draw(){
        Draw.alpha(parentAlpha);

        //draw selection box indicating placement position
        if(dragging != null && insertPosition <= seq.size){
            float shiftAmount = dragging.getWidth();
            float lastX = insertPosition == seq.size ? x + width : seq.get(insertPosition).x + x - space;
            float lastY = y;

            Tex.pane.draw(lastX - shiftAmount, lastY, dragging.getWidth(), height);
        }

        if(invalidated){
            children.each(c -> c.cullable = false);
        }

        super.draw();

        if(invalidated){
            children.each(c -> c.cullable = true);
            invalidated = false;
        }
    }

    void finishLayout(){
        if(dragging != null){
            //reset translation first
            for(Element child : getChildren()){
                child.setTranslation(0, 0);
            }
            clearChildren();

            //reorder things
            for(int i = 0; i <= insertPosition - 1 && i < seq.size; i++){
                addChild(seq.get(i));
            }

            addChild(dragging);

            for(int i = insertPosition; i < seq.size; i++){
                addChild(seq.get(i));
            }

            dragging = null;
        }

        layout();
        if (indexUpdater != null) indexUpdater.run();
    }
}