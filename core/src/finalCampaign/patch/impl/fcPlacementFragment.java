package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.*;
import arc.scene.Group;
import finalCampaign.event.*;
import finalCampaign.feature.blockShortcut.*;
import mindustry.input.*;
import mindustry.ui.fragments.*;

@Mixin(PlacementFragment.class)
public abstract class fcPlacementFragment {

    private static final fcPlacementFragBuildEvent buildEvent = new fcPlacementFragBuildEvent();

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
        buildEvent.parent = parent;
        buildEvent.instance = (PlacementFragment)(Object) this;
        Events.fire(buildEvent);
    }

}