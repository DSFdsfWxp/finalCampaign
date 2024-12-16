package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.world.modules.*;

@Mixin(PowerModule.class)
public abstract class fcPowerModule extends BlockModule {

    @Shadow(remap = false)
    public float status;
    @Shadow(remap = false)
    public IntSeq links;

    @Override
    public void read(Reads read){
        links.clear();
        short amount = read.s();
        for(int i = 0; i < amount; i++){
            links.add(read.i());
        }
        status = read.f();
        if(Float.isNaN(status)) status = 0f;
    }
}
