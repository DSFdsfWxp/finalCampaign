package finalCampaign.patch.impl;

import mindustry.ui.*;
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

    private final fcPlacementFragBuildEvent buildEvent = new fcPlacementFragBuildEvent();
    private final fcPlacementFragHoveredUpdateEvent hoveredUpdateEvent = new fcPlacementFragHoveredUpdateEvent();

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

    @Inject(method = "hovered", at = @At("RETURN"), cancellable = true, remap = false)
    private void fcHovered(CallbackInfoReturnable<Displayable> ci) {
        hoveredUpdateEvent.current = ci.getReturnValue();
        hoveredUpdateEvent.replace = ci::setReturnValue;
        Events.fire(hoveredUpdateEvent);
    }

}