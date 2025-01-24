package finalCampaign.event;

import arc.input.*;

public class fcInputKeyComboTapEvent {
    public KeyCode keycode;
    public int count;

    public void form(KeyCode keycode, int count) {
        this.keycode = keycode;
        this.count = count;
    }
}
