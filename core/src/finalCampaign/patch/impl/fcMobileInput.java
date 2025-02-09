package finalCampaign.patch.impl;

import arc.*;
import arc.input.*;
import arc.math.geom.*;
import arc.scene.Group;
import arc.scene.ui.layout.*;
import finalCampaign.event.*;
import mindustry.gen.*;
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
    private final fcInputHandlePinchEvent pinchEvent = new fcInputHandlePinchEvent();
    private final fcInputHandlePinchStopEvent pinchStopEvent = new fcInputHandlePinchStopEvent();
    private final fcInputHandlePanEvent panEvent = new fcInputHandlePanEvent();
    private final fcInputHandleLongPressEvent longPressEvent = new fcInputHandleLongPressEvent();
    private final fcInputHandleBuildUIEvent buildUIEvent = new fcInputHandleBuildUIEvent();
    private final fcInputHandleBuildPlacementUIEvent buildPlacementUIEvent = new fcInputHandleBuildPlacementUIEvent();

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

    @Inject(method = "tap", at = @At("HEAD"), remap = false, cancellable = true)
    private void fcTapHead(float x, float y, int count, KeyCode button, CallbackInfoReturnable<Boolean> ci) {
        tapEvent.form(x, y, count, button, true, ci::setReturnValue);
        Events.fire(tapEvent);
    }

    @Inject(method = "tap", at = @At("RETURN"), remap = false, cancellable = true)
    private void fcTapReturn(float x, float y, int count, KeyCode button, CallbackInfoReturnable<Boolean> ci) {
        tapEvent.form(x, y, count, button, false, ci::setReturnValue);
        Events.fire(tapEvent);
    }

    @Inject(method = "update", at = @At("HEAD"), remap = false)
    private void fcUpdateHead(CallbackInfo ci) {
        updateEvent.atHead = true;
        Events.fire(updateEvent);
    }

    @Inject(method = "update", at = @At("RETURN"), remap = false)
    private void fcUpdateReturn(CallbackInfo ci) {
        updateEvent.atHead = false;
        Events.fire(updateEvent);
    }

    @Inject(method = "updateMovement", at = @At("RETURN"), remap = false)
    private void fcUpdateMovement(Unit unit, CallbackInfo ci) {
        updateMovementEvent.unit = unit;
        Events.fire(updateMovementEvent);
    }

    public boolean pinch(Vec2 initialPointer1, Vec2 initialPointer2, Vec2 pointer1, Vec2 pointer2) {
        pinchEvent.form(initialPointer1, initialPointer2, pointer1, pointer2);
        Events.fire(pinchEvent);
        return false;
    }

    public void pinchStop() {
        Events.fire(pinchStopEvent);
    }

    @Inject(method = "pan", at = @At("HEAD"), cancellable = true, remap = false)
    private void fcPanHead(float x, float y, float deltaX, float deltaY, CallbackInfoReturnable<Boolean> ci) {
        panEvent.form(x, y, deltaX, deltaY, true, ci::setReturnValue);
        Events.fire(panEvent);
    }

    @Inject(method = "pan", at = @At("RETURN"), cancellable = true, remap = false)
    private void fcPanReturn(float x, float y, float deltaX, float deltaY, CallbackInfoReturnable<Boolean> ci) {
        panEvent.form(x, y, deltaX, deltaY, false, ci::setReturnValue);
        Events.fire(panEvent);
    }

    @Inject(method = "longPress", at = @At("HEAD"), cancellable = true, remap = false)
    private void fcLongPressHead(float x, float y, CallbackInfoReturnable<Boolean> ci) {
        longPressEvent.form(x, y, true, ci::setReturnValue);
        Events.fire(longPressEvent);
    }

    @Inject(method = "longPress", at = @At("RETURN"), cancellable = true, remap = false)
    private void fcLongPressReturn(float x, float y, CallbackInfoReturnable<Boolean> ci) {
        longPressEvent.form(x, y, false, ci::setReturnValue);
        Events.fire(longPressEvent);
    }

    @Inject(method = "buildUI", at = @At("RETURN"), remap = false)
    private void fcBuildUI(Group group, CallbackInfo ci) {
        buildUIEvent.group = group;
        Events.fire(buildUIEvent);
    }

    @Inject(method = "buildPlacementUI", at = @At("RETURN"), remap = false)
    private void fcBuildPlacementUI(Table table, CallbackInfo ci) {
        buildPlacementUIEvent.table = table;
        Events.fire(buildPlacementUIEvent);
    }
}
