package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.input.*;
import arc.struct.*;
import finalCampaign.input.fcInputHook.*;
import finalCampaign.patch.*;

@Mixin(KeyboardDevice.class)
public abstract class fcKeyboardDevice extends InputDevice implements InputProcessor, IFcKeyboardDevice {
    private static Seq<IBooleanInputHook> pressedHooks = new Seq<>();
    private static Seq<IBooleanInputHook> tappedHooks = new Seq<>();
    private static Seq<IBooleanInputHook> releasedHooks = new Seq<>();
    private static Seq<IFloatInputHook> axisHooks = new Seq<>();

    @Shadow(remap = false)
    private IntSet pressed;
    @Shadow(remap = false)
    private IntSet lastFramePressed;
    @Shadow(remap = false)
    private IntSet justPressed;
    @Shadow(remap = false)
    private IntFloatMap axes;

    @Inject(method = "isPressed", remap = false, cancellable = true, at = @At("RETURN"))
    public void fcIsPressed(KeyCode key, CallbackInfoReturnable<Boolean> ci) {
        boolean v = ci.getReturnValueZ();
        if (key != null) for (IBooleanInputHook hook : pressedHooks) v = hook.handle(key, v);
        ci.setReturnValue(v);
    }

    @Inject(method = "isTapped", remap = false, cancellable = true, at = @At("RETURN"))
    public void fcIsTapped(KeyCode key, CallbackInfoReturnable<Boolean> ci) {
        boolean v = ci.getReturnValueZ();
        if (key != null) for (IBooleanInputHook hook : tappedHooks) v = hook.handle(key, v);
        ci.setReturnValue(v);
    }

    @Inject(method = "isReleased", remap = false, cancellable = true, at = @At("RETURN"))
    public void fcIsReleased(KeyCode key, CallbackInfoReturnable<Boolean> ci) {
        boolean v = ci.getReturnValueZ();
        if (key != null) for (IBooleanInputHook hook : releasedHooks) v = hook.handle(key, v);
        ci.setReturnValue(v);
    }

    @Inject(method = "getAxis", remap = false, cancellable = true, at = @At("RETURN"))
    public void fcGetAxis(KeyCode keyCode, CallbackInfoReturnable<Float> ci) {
        float v = ci.getReturnValueF();
        if (keyCode != null) for (IFloatInputHook hook : axisHooks) v = hook.handle(keyCode, v);
        ci.setReturnValue(v);
    }

    public void fcInstallHook(inputHookPoint point, IBooleanInputHook hook) {
        if (point == inputHookPoint.pressed) {
            if (!pressedHooks.contains(hook)) pressedHooks.add(hook);
        } else if (point == inputHookPoint.released) {
            if (!releasedHooks.contains(hook)) releasedHooks.add(hook);
        } else if (point == inputHookPoint.tapped) {
            if (!tappedHooks.contains(hook)) tappedHooks.add(hook);
        }
    }

    public void fcInstallAxisHook(IFloatInputHook hook) {
        if (!axisHooks.contains(hook)) axisHooks.add(hook);
    }

    public boolean fcRealIsPressed(KeyCode key) {
        if (key == null) return false;
        if(key == KeyCode.anyKey) return pressed.size > 0;

        return pressed.contains(key.ordinal());
    }

    public boolean fcRealIsTapped(KeyCode key) {
        if (key == null) return false;
        return justPressed.contains(key.ordinal());
    }

    public boolean fcRealIsReleased(KeyCode key) {
        if (key == null) return false;
        return !fcRealIsPressed(key) && lastFramePressed.contains(key.ordinal());
    }

    public float fcGetRealAxis(KeyCode keyCode) {
        if (keyCode == null) return 0f;
        return axes.get(keyCode.ordinal(), 0);
    }
}
