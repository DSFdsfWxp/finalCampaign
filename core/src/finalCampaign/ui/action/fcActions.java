package finalCampaign.ui.action;

import arc.scene.actions.*;

public class fcActions {

    public static keepCenterAction keepCenter(float time) {
        var action = Actions.action(keepCenterAction.class, keepCenterAction::new);
        action.setTime(time);
        return action;
    }
}
