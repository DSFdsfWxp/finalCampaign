package finalCampaign.util.event;

import arc.scene.*;
import arc.scene.event.*;

public class forwardEventListener implements EventListener {
    Element target;

    public forwardEventListener(Element target) {
        this.target = target;
    }

    public boolean handle(SceneEvent event) {
        return target.fire(event);
    }
}