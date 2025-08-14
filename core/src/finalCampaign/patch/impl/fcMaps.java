package finalCampaign.patch.impl;

import arc.struct.*;
import mindustry.maps.*;
import finalCampaign.map.fcMap;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Maps.class)
public class fcMaps {
    @Inject(method = "saveMap", at = @At("HEAD"), remap = false)
    private void fcBeforeSaveMap(ObjectMap<String, String> baseTags, CallbackInfoReturnable<Map> ci) {
        fcMap.savingMap = true;
    }

    @Inject(method = "saveMap", at = @At("RETURN"), remap = false)
    private void fcAfterSaveMap(ObjectMap<String, String> baseTags, CallbackInfoReturnable<Map> ci) {
        fcMap.savingMap = false;
    }
}
