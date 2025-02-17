package finalCampaign.patch.impl;

import arc.*;
import arc.scene.ui.layout.*;
import finalCampaign.event.*;
import mindustry.gen.*;
import org.spongepowered.asm.mixin.*;
import finalCampaign.patch.*;
import mindustry.type.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(UnitType.class)
public abstract class fcUnitType implements IFcUnitType, IFcAttractiveEntityType {

    public float fcAttractiveness = 0f;
    public float fcAntiAttractiveness = 0f;
    public float fcHiddenness = 0f;

    private final fcEntityDisplayInfoEvent displayInfoEvent = new fcEntityDisplayInfoEvent();

    public float fcAttractiveness() {
        return fcAttractiveness;
    }

    public float fcAntiAttractiveness() {
        return fcAntiAttractiveness;
    }

    public float fcHiddenness() {
        return fcHiddenness;
    }

    @Inject(method = "display", at = @At("HEAD"), remap = false)
    private void fcDisplayBefore(Unit unit, Table table, CallbackInfo ci) {
        displayInfoEvent.form(unit, table, true);
        Events.fire(displayInfoEvent);

    }

    @Inject(method = "display", at = @At("RETURN"), remap = false)
    private void fcDisplayAfter(Unit unit, Table table, CallbackInfo ci) {
        displayInfoEvent.form(unit, table, false);
        Events.fire(displayInfoEvent);
    }
}
