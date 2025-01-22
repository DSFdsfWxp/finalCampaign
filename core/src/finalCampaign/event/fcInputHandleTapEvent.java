package finalCampaign.event;

import arc.func.*;
import arc.input.*;

public class fcInputHandleTapEvent {
    public float x, y;
    public int count;
    public KeyCode button;
    public boolean atHead;
    public Cons<Boolean> cancel;

    public void form(float x, float y, int count, KeyCode button, boolean atHead, Cons<Boolean> cancel) {
        this.x = x;
        this.y = y;
        this.count = count;
        this.button = button;
        this.atHead = atHead;
        this.cancel = cancel;
    }
}
