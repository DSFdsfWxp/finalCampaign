package finalCampaign.event;

import arc.math.geom.*;

public class fcInputHandlePinchEvent {
    public Vec2 initialPointer1, initialPointer2;
    public Vec2 pointer1, pointer2;

    public void form(Vec2 initialPointer1, Vec2 initialPointer2, Vec2 pointer1, Vec2 pointer2) {
        this.initialPointer1 = initialPointer1;
        this.initialPointer2 = initialPointer2;
        this.pointer1 = pointer1;
        this.pointer2 = pointer2;
    }
}
