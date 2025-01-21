package finalCampaign.patch.impl;

import arc.Events;
import arc.input.KeyCode;
import finalCampaign.event.*;
import mindustry.gen.Unit;
import mindustry.input.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(MobileInput.class)
public abstract class fcMobileInput {

    private final fcDrawWorldTopEvent drawTopEvent = new fcDrawWorldTopEvent();
    private final fcDrawWorldBottomEvent drawBottomEvent = new fcDrawWorldBottomEvent();
    private final fcDrawWorldOverSelectEvent drawOverSelectEvent = new fcDrawWorldOverSelectEvent();
    private final fcInputHandleUpdateStateEvent updateStateEvent = new fcInputHandleUpdateStateEvent();
    private final fcInputHandleTapEvent tapEvent = new fcInputHandleTapEvent();
    private final fcInputHandleUpdateEvent updateEvent = new fcInputHandleUpdateEvent();
    private final fcInputHandleUpdateMovementEvent updateMovementEvent = new fcInputHandleUpdateMovementEvent();

    @Inject(method = "drawTop", at = @At("RETURN"), remap = false)
    private void fcDrawTop(CallbackInfo ci) {
        Events.fire(drawTopEvent);
    }

    @Inject(method = "drawBottom", at = @At("RETURN"), remap = false)
    private void fcDrawBottom(CallbackInfo ci) {
        Events.fire(drawBottomEvent);
    }

    @Inject(method = "drawOverSelect", at = @At("RETURN"), remap = false)
    private void fcDrawOverSelect(CallbackInfo ci) {
        Events.fire(drawOverSelectEvent);
    }

    @Inject(method = "updateState", at = @At("RETURN"), remap = false)
    private void fcUpdateState(CallbackInfo ci) {
        Events.fire(updateStateEvent);
    }

    @Inject(method = "tap", at = @At("RETURN"), remap = false)
    private void fcTap(float x, float y, int count, KeyCode button, CallbackInfoReturnable<Boolean> ci) {
        tapEvent.form(x, y, count, button, ci::setReturnValue);
        Events.fire(tapEvent);
    }

    @Inject(method = "update", at = @At("RETURN"), remap = false)
    private void fcUpdate(CallbackInfo ci) {
        Events.fire(updateEvent);
    }

    @Inject(method = "updateMovement", at = @At("RETURN"), remap = false)
    private void fcUpdateMovement(Unit unit, CallbackInfo ci) {
        updateMovementEvent.unit = unit;
        Events.fire(updateMovementEvent);
    }
}
