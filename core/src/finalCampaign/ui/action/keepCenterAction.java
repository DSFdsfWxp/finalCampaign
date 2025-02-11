package finalCampaign.ui.action;

import arc.scene.*;

public class keepCenterAction extends Action {

    private float centerX, centerY;
    private float time;

    public keepCenterAction() {
        centerX = actor.x + actor.getWidth() / 2f;
        centerY = actor.y + actor.getHeight() / 2f;
    }

    public void setTime(float time) {
        this.time = time;
    }

    @Override
    public boolean act(float delta) {
        actor.x = centerX - actor.getWidth() / 2f;
        actor.y = centerY - actor.getHeight() / 2f;

        return (time -= delta) <= 0f;
    }
}
