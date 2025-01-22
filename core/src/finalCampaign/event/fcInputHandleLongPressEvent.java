package finalCampaign.event;

import arc.func.Cons;

public class fcInputHandleLongPressEvent {
    public float x, y;
    public boolean atHead;
    public Cons<Boolean> cancel;

    public void form(float x, float y, boolean atHead, Cons<Boolean> cancel) {
        this.x = x;
        this.y = y;
        this.atHead = atHead;
        this.cancel = cancel;
    }
}
