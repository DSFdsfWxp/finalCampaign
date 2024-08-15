package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import finalCampaign.patch.*;
import mindustry.world.blocks.defense.turrets.*;

@Mixin(Turret.class)
public abstract class fcTurret implements IFcTurret {
    private byte[] fcSortfData;
    private boolean fcPreferBuildingTarget = false;

    public byte[] fcSortfData() {
        return fcSortfData;
    }

    public void fcSortf(byte[] v) {
        fcSortfData = v;
    }

    public boolean fcPreferBuildingTarget() {
        return fcPreferBuildingTarget;
    }

    public void fcPreferBuildingTarget(boolean v) {
        fcPreferBuildingTarget = v;
    }
}
