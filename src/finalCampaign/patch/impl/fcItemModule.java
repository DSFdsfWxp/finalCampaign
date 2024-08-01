package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.type.*;
import mindustry.world.modules.*;

@Mixin(ItemModule.class)
public abstract class fcItemModule extends BlockModule {
    
    @Shadow(remap = false)
    protected int[] items;
    @Shadow(remap = false)
    protected int total;
    @Shadow(remap = false)
    protected int takeRotation;
    @Shadow(remap = false)
    private @Nullable WindowedMean[] flow;
    @Shadow(remap = false)
    private static float[] cacheSums;

    @Nullable
    public Item take(){
        for(int i = 0; i < items.length; i++){
            int index = (i + takeRotation);
            if(index >= items.length) index -= items.length;
            if(items[index] > 0){
                if (items[index] != Integer.MAX_VALUE) {
                    items[index] --;
                    total --;
                }
                takeRotation = index + 1;
                return Vars.content.item(index);
            }
        }
        return null;
    }

    @Inject(method = "add", at = @At("HEAD"), remap = false, cancellable = true)
    private void fcAdd(int item, int amount, CallbackInfo ci){
        if (items[item] == Integer.MAX_VALUE) ci.cancel();
    }

    @Inject(method = {"set", "remove"}, at = @At("HEAD"), remap = false, cancellable = true)
    public void fcItemChange(Item item, int amount, CallbackInfo ci){
        if (items[item.id] == Integer.MAX_VALUE) ci.cancel();
        if (amount == Integer.MAX_VALUE) {
            items[item.id] = amount;
            ci.cancel();
        }
    }


}
