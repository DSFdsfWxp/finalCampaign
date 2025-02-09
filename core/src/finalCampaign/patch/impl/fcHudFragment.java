package finalCampaign.patch.impl;

import arc.*;
import arc.scene.Group;
import finalCampaign.event.*;
import mindustry.ui.fragments.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(HudFragment.class)
public class fcHudFragment {

    private static final fcHudFragBuildEvent buildEvent = new fcHudFragBuildEvent();

    @Inject(method = "build", at = @At("RETURN"), remap = false)
    private void fcBuild(Group parent, CallbackInfo ci) {
        buildEvent.parent = parent;
        Events.fire(buildEvent);
    }
}
