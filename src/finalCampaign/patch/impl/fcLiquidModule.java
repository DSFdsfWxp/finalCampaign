package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.util.io.*;
import finalCampaign.feature.featureClass.mapVersion.*;
import finalCampaign.patch.*;
import mindustry.*;
import mindustry.io.*;
import mindustry.type.*;
import mindustry.world.modules.*;

@Mixin(LiquidModule.class)
public abstract class fcLiquidModule extends BlockModule implements IFcLiquidModule {

    @Shadow(remap = false)
    private Liquid current;

    @Shadow(remap = false)
    public abstract float get(Liquid liquid);

    @Shadow(remap = false)
    public abstract float currentAmount();


    @Inject(method = "read", at = @At("RETURN"), remap = false)
    public void fcRead(Reads read, boolean legacy, CallbackInfo ci) {
        if (fMapVersion.currentVersion() < 1) return;
        Liquid fcCurrent = TypeIO.readLiquid(read);
        if (fcCurrent == null) return;
        if (get(fcCurrent) <= 0f) return;
        current = fcCurrent;
    }

    @Inject(method = "write", at = @At("RETURN"), remap = false)
    public void fcWrite(Writes write, CallbackInfo ci) {
        TypeIO.writeLiquid(write, current);
    }

    public void fcFindNextAvailable() {
        if (currentAmount() > 0f) return;
        for (Liquid liquid : Vars.content.liquids()) {
            if (get(liquid) > 0f) {
                current = liquid;
                break;
            }
        }
    }

}
