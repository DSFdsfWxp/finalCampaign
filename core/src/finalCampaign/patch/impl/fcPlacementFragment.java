package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.scene.*;
import arc.scene.Group;
import arc.scene.ui.layout.*;
import arc.util.*;
import finalCampaign.feature.blockShortcut.*;
import finalCampaign.feature.setMode.*;
import mindustry.*;
import mindustry.input.*;
import mindustry.ui.fragments.*;

@Mixin(PlacementFragment.class)
public abstract class fcPlacementFragment {
    @Shadow(remap = false)
    private Table topTable;

    private boolean rebuildCategoryNeeded = false;

    public void fcRebuildCategory() {
        rebuildCategoryNeeded = true;
    }

    @Inject(method = "gridUpdate", at = @At("HEAD"), remap = false, cancellable = true)
    private void fcGridUpdate(InputHandler input, CallbackInfoReturnable<Boolean> ci) {
        if (!rebuildCategoryNeeded) {
            if (fBlockShortcut.disableGameBlockSelect())
                ci.setReturnValue(false);
            return;
        }

        rebuildCategoryNeeded = false;
        ci.setReturnValue(true);
    }

    @Inject(method = "build", at = @At("RETURN"), remap = false)
    private void fcBuild(Group parent, CallbackInfo ci) {
        Table full = (Table) parent.getChildren().peek();
        var originalVisible = full.visibility;

        full.name = "fcPlacementFragment";
        full.visible(() -> originalVisible.get() && !fSetMode.isOn());

        // remove shortcut keys' hint
        Runnable originalUpdate = Reflect.get(Element.class, topTable.getChildren().first(), "update");
        topTable.getChildren().first().update(() -> {
            if (fBlockShortcut.disableGameBlockSelect()) {
                boolean isMobile = Vars.mobile;
                Vars.mobile = true;
    
                originalUpdate.run();
    
                Vars.mobile = isMobile;
            } else {
                originalUpdate.run();
            }
        });
    }

}