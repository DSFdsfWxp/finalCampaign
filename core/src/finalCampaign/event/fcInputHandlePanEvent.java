package finalCampaign.event;

import arc.func.*;

public class fcInputHandlePanEvent {
    public float x, y;
    public float deltaX, deltaY;
    public boolean atHead;
    public Cons<Boolean> cancel;

    public void form(float x, float y, float deltaX, float deltaY, boolean atHead, Cons<Boolean> cancel) {
        this.x = x;
        this.y = y;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.atHead = atHead;
        this.cancel = cancel;
    }
}
