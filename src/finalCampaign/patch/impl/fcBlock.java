package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.scene.style.*;
import arc.struct.*;
import finalCampaign.bundle;
import finalCampaign.feature.featureClass.display.barDetail.*;
import finalCampaign.graphics.*;
import finalCampaign.patch.*;
import finalCampaign.ui.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;

@Mixin(Block.class)
public abstract class fcBlock implements IFcBlock, IFcAttractiveEntityType {
    @Shadow(remap = false)
    protected OrderedMap<String, Func<Building, Bar>> barMap;

    public float fcAttractiveness = 0f;
    public float fcAntiAttractiveness = 0f;
    public float fcHiddenness = 0f;

    public float fcAttractiveness() {
        return fcAttractiveness;
    }

    public float fcAntiAttractiveness() {
        return fcAntiAttractiveness;
    }

    public float fcHiddenness() {
        return fcHiddenness;
    }

    public OrderedMap<String, Func<Building, Bar>> fcBarMap() {
        return barMap;
    }

    @Inject(method = "addBar", at = @At("HEAD"), cancellable = true, remap = false)
    public <T extends Building> void fcAddBar(String name, Func<T, Bar> sup, CallbackInfo ci) {
        if (!fBarDetail.isOn()) return;

        if (name.equals("health")) {
            barMap.put(name, b -> new fakeBar(new detailBar(b.maxHealth, 
                                              () -> b.health, 
                                              icons.health, 
                                              Core.bundle.get("stat.health"), 
                                              Pal.health)).blink(Color.white));
            ci.cancel();
        } else if (name.startsWith("liquid-")) {
            Liquid liquid = Vars.content.liquid(name.substring(7));
            barMap.put(name, b -> new fakeBar(new detailBar(b.block.liquidCapacity, 
                                              () -> b.liquids.get(liquid), 
                                              new TextureRegionDrawable(liquid.fullIcon), 
                                              liquid.localizedName, 
                                              liquid.color)));
            ci.cancel();
        } else if (name.equals("items")) {
            barMap.put(name, b -> new fakeBar(new detailBar(b.block.itemCapacity, 
                                              () -> (float)b.items.total(), 
                                              icons.totalItem, 
                                              bundle.get("bar.totalItem"), 
                                              Pal.items)));
            ci.cancel();
        }
    }
}
