package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.math.*;
import arc.util.io.*;
import finalCampaign.patch.*;
import mindustry.gen.*;
import mindustry.logic.*;

@Mixin(Building.class)
public abstract class fcBuilding implements IFcBuilding{
    private boolean fcSetModeSelected = false;
    private boolean fcForceDisable = false;
    private boolean fcForceEnable = false;
    private String fcStatus = "reqMissing";
    private boolean fcInfinityPower = false;

    @Shadow(remap = false)
    boolean enabled;

    public boolean fcSetModeSelected() {
        return fcSetModeSelected;
    }

    public void fcSetModeSelected(boolean selected) {
        fcSetModeSelected = selected;
    }

    public boolean fcForceDisable() {
        return fcForceDisable;
    }

    public void fcForceDisable(boolean v) {
        fcForceDisable = v;
    }

    public boolean fcForceEnable() {
        return fcForceEnable;
    }

    public void fcForceEnable(boolean v) {
        fcForceEnable = v;
    }

    public String fcStatus() {
        return fcStatus;
    }

    public void fcStatus(String v) {
        fcStatus = v;
    }

    public boolean fcInfinityPower() {
        return fcInfinityPower;
    }

    public void fcInfinityPower(boolean v) {
        fcInfinityPower = v;
    }

    @Inject(method = "writeBase", at = @At("RETURN"), remap = false)
    public void fcWriteBase(Writes write, CallbackInfo ci) {
        write.bool(fcForceDisable);
        write.bool(fcForceEnable);
        write.bool(fcInfinityPower);
    }

    @Inject(method = "readBase", at = @At("RETURN"), remap = false)
    public void fcReadBase(Reads read, CallbackInfo ci) {
        fcForceDisable = read.bool();
        fcForceEnable = read.bool();
        fcInfinityPower = read.bool();
    }

    public void control(LAccess type, double p1, double p2, double p3, double p4){
        if(type == LAccess.enabled && !fcForceDisable && !fcForceEnable){
            enabled = !Mathf.zero((float)p1);
        }
    }

    @Inject(method = "update", at = @At("HEAD"), remap = false)
    public void fcUpdate() {
        if (fcForceDisable || fcForceEnable) enabled = fcForceEnable;
    }
}
