package finalCampaign.input;

import arc.*;
import finalCampaign.event.*;

public class fcInput {
    public static void load() {
        fcInputHook.init();
        fcBindings.load();
        Core.app.addListener(new inputHandleUpdater());
    }

    public static class inputHandleUpdater implements ApplicationListener {
        private fcInputHandleUpdateEvent event = new fcInputHandleUpdateEvent();

        @Override
        public void update() {
            Events.fire(event);
        }
    }
}
