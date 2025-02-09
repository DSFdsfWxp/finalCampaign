package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.*;
import arc.input.*;
import arc.scene.Group;
import arc.scene.ui.layout.*;
import finalCampaign.event.*;
import mindustry.gen.*;
import mindustry.input.*;

@Mixin(DesktopInput.class)
public abstract class fcDesktopInput extends InputHandler {
    
    private final fcDrawWorldTopEvent drawTopEvent = new fcDrawWorldTopEvent();
    private final fcDrawWorldBottomEvent drawBottomEvent = new fcDrawWorldBottomEvent();
    private final fcDrawWorldOverSelectEvent drawOverSelectEvent = new fcDrawWorldOverSelectEvent();
    private final fcInputHandleUpdateStateEvent updateStateEvent = new fcInputHandleUpdateStateEvent();
    private final fcInputHandleTapEvent tapEvent = new fcInputHandleTapEvent();
    private final fcInputHandleUpdateEvent updateEvent = new fcInputHandleUpdateEvent();
    private final fcInputHandleUpdateMovementEvent updateMovementEvent = new fcInputHandleUpdateMovementEvent();
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

    @Inject(method = "updateState", at = @At("RETURN"), remap = false)
    private void fcUpdateState(CallbackInfo ci) {
        Events.fire(updateStateEvent);
    }

    @Override
    public void drawOverSelect() {
        super.drawOverSelect();
        Events.fire(drawOverSelectEvent);
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
