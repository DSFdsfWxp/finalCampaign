package finalCampaign.ui.event;

import arc.input.*;
import arc.scene.event.*;

/** Fixed from {@link DragListener} */
public class fcDragListener extends InputListener {
    private float tapSquareSize = 0, touchDownX = -1, touchDownY = -1, stageTouchDownX = -1, stageTouchDownY = -1;
    private int pressedPointer = -1;
    private KeyCode button = KeyCode.mouseLeft;
    private boolean dragging;
    private float deltaX, deltaY;

    public fcDragListener() {}

    public fcDragListener(float tapSquareSize, KeyCode button) {
        this.tapSquareSize = tapSquareSize;
        this.button = button;
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
        if(pressedPointer != -1) return false;
        if(pointer == 0 && button != this.button) return false;
        pressedPointer = pointer;
        touchDownX = x;
        touchDownY = y;
        stageTouchDownX = event.stageX;
        stageTouchDownY = event.stageY;
        return true;
    }

    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer){
        if(pointer != pressedPointer) return;
        if(!dragging && (Math.abs(touchDownX - x) > tapSquareSize || Math.abs(touchDownY - y) > tapSquareSize)){
            dragging = true;
            dragStart(event, x, y, pointer);
            deltaX = x;
            deltaY = y;
        }
        if(dragging){
            deltaX -= x;
            deltaY -= y;
            drag(event, x, y, pointer);
            deltaX = x;
            deltaY = y;
        }
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
        if(pointer == pressedPointer){
            if(dragging) dragStop(event, x, y, pointer);
            cancel();
        }
    }

    public void dragStart(InputEvent event, float x, float y, int pointer){
    }

    public void drag(InputEvent event, float x, float y, int pointer){
    }

    public void dragStop(InputEvent event, float x, float y, int pointer){
    }

    /* If a drag is in progress, no further drag methods will be called until a new drag is started. */
    public void cancel(){
        dragging = false;
        pressedPointer = -1;
    }

    /** Returns true if a touch has been dragged outside the tap square. */
    public boolean isDragging(){
        return dragging;
    }

    public float getTapSquareSize(){
        return tapSquareSize;
    }

    public void setTapSquareSize(float halfTapSquareSize){
        tapSquareSize = halfTapSquareSize;
    }

    public float getTouchDownX(){
        return touchDownX;
    }

    public float getTouchDownY(){
        return touchDownY;
    }

    public float getStageTouchDownX(){
        return stageTouchDownX;
    }

    public float getStageTouchDownY(){
        return stageTouchDownY;
    }

    /** Returns the amount on the x axis that the touch has been dragged since the last drag event. */
    public float getDeltaX(){
        return deltaX;
    }

    /** Returns the amount on the y axis that the touch has been dragged since the last drag event. */
    public float getDeltaY(){
        return deltaY;
    }

    public KeyCode getButton(){
        return button;
    }

    /** Sets the button to listen for, all other buttons are ignored. Default is {@link KeyCode#mouseLeft}. Use -1 for any button. */
    public void setButton(KeyCode button){
        this.button = button;
    }
}
