package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import mindustry.core.*;
import mindustry.game.*;
import mindustry.maps.*;

@Mixin(World.class)
public abstract class fcWorld {
    @Inject(method = "loadMap(Lmindustry/maps/Map;Lmindustry/game/Rules;)V", at = @At("RETURN"), remap = false)
    private void fcLoadMap(Map map, Rules checkRules, CallbackInfo ci) {
        int ordinal = Integer.parseInt(map.tags.get("finalCampaign.appliedGamemode", "-1"));
        finalCampaign.map.fcMap.initialMode = ordinal == -1 ? null : Gamemode.all[ordinal];
    }
}
