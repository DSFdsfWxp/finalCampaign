package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import finalCampaign.patch.*;
import mindustry.world.blocks.defense.turrets.*;

@Mixin(LiquidTurret.class)
public class fcLiquidTurret implements IFcLiquidTurret {
    private boolean fcPreferExtinguish = true;

    public boolean fcPreferExtinguish() {
        return fcPreferExtinguish;
    }

    public void fcPreferExtinguish(boolean v) {
        fcPreferExtinguish = v;
    }
}
