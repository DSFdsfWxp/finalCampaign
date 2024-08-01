package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import finalCampaign.patch.*;
import mindustry.gen.*;

@Mixin(Building.class)
public class fcBuilding implements IFcBuilding{
    private boolean fcSetModeSelected = false;
    private boolean fcForceDisable = false;
    private String fcStatus = "reqMissing";
    private boolean fcInfinityPower = false;

    public boolean fcSetModeSelected() {
        return fcSetModeSelected;
    }

    public void fcSetModeSelected(boolean selected) {
        fcSetModeSelected = selected;
    }

    public boolean fcForceDisable() {
        return fcForceDisable;
    }

    public void fcForceDisable(boolean v) {
        fcForceDisable = v;
    }

    public String fcStatus() {
        return fcStatus;
    }

    public void fcStatus(String v) {
        fcStatus = v;
    }

    public boolean fcInfinityPower() {
        return fcInfinityPower;
    }

    public void fcInfinityPower(boolean v) {
        fcInfinityPower = v;
    }
}
