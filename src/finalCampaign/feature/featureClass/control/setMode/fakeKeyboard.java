package finalCampaign.feature.featureClass.control.setMode;

import arc.input.*;

public class fakeKeyboard extends KeyboardDevice {
    @Override
    public boolean isPressed(KeyCode key){
        return super.isPressed(key);
    }

    @Override
    public boolean isTapped(KeyCode key){
        return super.isTapped(key);
    }

    @Override
    public boolean isReleased(KeyCode key){
        return super.isReleased(key);
    }

    @Override
    public float getAxis(KeyCode keyCode){
        return super.getAxis(keyCode);
    }
}
