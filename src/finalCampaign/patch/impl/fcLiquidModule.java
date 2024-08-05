package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.util.io.*;
import mindustry.io.*;
import mindustry.type.*;
import mindustry.world.modules.*;

@Mixin(LiquidModule.class)
public abstract class fcLiquidModule extends BlockModule {

    @Shadow(remap = false)
    private Liquid current;

    @Shadow(remap = false)
    public abstract float get(Liquid liquid);


    @Inject(method = "read", at = @At("RETURN"), remap = false)
    public void fcRead(Reads read, boolean legacy, CallbackInfo ci) {
        Liquid fcCurrent = TypeIO.readLiquid(read);
        if (fcCurrent == null) return;
        if (get(fcCurrent) <= 0f) return;
        current = fcCurrent;
    }

    @Inject(method = "write", at = @At("RETURN"), remap = false)
    public void fcWrite(Writes write, CallbackInfo ci) {
        TypeIO.writeLiquid(write, current);
    }

}
