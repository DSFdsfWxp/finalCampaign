package finalCampaign.feature.editMode;

import arc.*;
import finalCampaign.event.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.input.*;


public class logic {
    private static float lastPinchX = -1, lastPinchY = -1;
    private static float lastLongPressX = -1, lastLongPressY = -1;
    private static MobileInput input;

    @SuppressWarnings("unused")
    public static void gameStateChange(StateChangeEvent event) {
        if (Vars.control.input instanceof MobileInput mi)
            input = mi;
    }

    public static void pinch(fcInputHandlePinchEvent event) {
        if (!fEditMode.isOn())
            return;

        float x = (event.pointer1.x + event.pointer2.x) / 2f;
        float y = (event.pointer1.y + event.pointer2.y) / 2f;

        if (lastPinchX >= 0 && lastPinchY >= 0) {
            float scale = Core.camera.width / Core.graphics.getWidth();

            Core.camera.position.x -= (x - lastPinchX) * scale;
            Core.camera.position.y -= (y - lastPinchY) * scale;
            Core.camera.position.clamp(-Core.camera.width / 4f, -Core.camera.height / 4f, Vars.world.unitWidth() + Core.camera.width / 4f, Vars.world.unitHeight() + Core.camera.height / 4f);
        }

        lastPinchX = x;
        lastPinchY = y;
    }

    @SuppressWarnings("unused")
    public static void pinchStop(fcInputHandlePinchStopEvent event) {
        if (!fEditMode.isOn())
            return;

        lastPinchX = lastPinchY = -1;
    }

    public static void pan(fcInputHandlePanEvent event) {
        if (!fEditMode.isOn() || !event.atHead)
            return;

        if (!input.selecting || !input.detector.isLongPressed()) {
            event.cancel.get(false);
            return;
        }

        if (input.mode == PlaceMode.placing) {
            input.longPress(event.x, event.y);
            event.cancel.get(false);
        }
    }

    public static void longPress(fcInputHandleLongPressEvent event) {
        if (!fEditMode.isOn() || !event.atHead)
            return;

        if (input.mode == PlaceMode.placing && !input.detector.isPanning()) {
            event.cancel.get(false);
            return;
        }

        if (input.selecting) {
            if (lastLongPressX >= 0 || lastLongPressY >=0)
                input.pan(event.x, event.y, lastLongPressX - event.x, lastLongPressY - event.y);

            lastLongPressX = event.x;
            lastLongPressY = event.y;

            event.cancel.get(false);
        }
    }

    @SuppressWarnings("unused")
    public static void update(fcInputHandleUpdateEvent event) {
        if (!fEditMode.isOn())
            return;

        if (!input.selecting)
            lastLongPressX = lastLongPressY = -1;
    }
}
