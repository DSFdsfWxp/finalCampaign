package finalCampaign.patch;

import arc.input.*;
import finalCampaign.input.fcInputHook.*;

public interface IFcKeyboardDevice {
    public void fcInstallHook(inputHookPoint point, IBooleanInputHook hook);
    public void fcInstallAxisHook(IFloatInputHook hook);

    public boolean fcRealIsPressed(KeyCode key);
    public boolean fcRealIsTapped(KeyCode key);
    public boolean fcRealIsReleased(KeyCode key);
    public float fcGetRealAxis(KeyCode keyCode);
}
