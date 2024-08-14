package finalCampaign.util;

import arc.scene.event.*;

public class notBubblesInputListener extends InputListener {
    @Override
    public boolean handle(SceneEvent e) {
        e.bubbles = false;
        return super.handle(e);
    }
}
