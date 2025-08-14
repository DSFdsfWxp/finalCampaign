package finalCampaign.patch.impl;

import arc.*;
import arc.scene.ui.layout.*;
import finalCampaign.event.*;
import finalCampaign.map.*;
import finalCampaign.map.fcMap;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import finalCampaign.patch.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.world.modules.*;

@Mixin(Building.class)
public abstract class fcBuilding implements IFcBuilding {

    private boolean fcSetModeSelected = false;
    private boolean fcForceDisable = false;
    private boolean fcForceEnable = false;
    private String fcStatus = "reqMissing";
    private boolean fcInfinityPower = false;

    private final fcEntityDisplayInfoEvent displayInfoEvent = new fcEntityDisplayInfoEvent();

    @Shadow(remap = false)
    public boolean enabled;
    @Shadow(remap = false)
    public float health, maxHealth;
    @Shadow(remap = false)
    public PowerModule power;

    @Shadow(remap = false)
    public abstract void kill();

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
        if (fcMap.exportingPlainSave) return;
        write.bool(fcForceDisable);
        write.bool(fcForceEnable);
        write.bool(fcInfinityPower);
        write.bool(health == Float.POSITIVE_INFINITY);
    }

    @Inject(method = "readBase", at = @At("RETURN"), remap = false)
    public void fcReadBase(Reads read, CallbackInfo ci) {
        if (fcMap.currentVersion < 1) return;
        fcForceDisable = read.bool();
        fcForceEnable = read.bool();
        fcInfinityPower = read.bool();
        if (read.bool()) health = Float.POSITIVE_INFINITY;
    }

    @Inject(method = "control(Lmindustry/logic/LAccess;DDDD)V", at = @At("RETURN"), remap = false)
    private void fcControl(LAccess type, double p1, double p2, double p3, double p4, CallbackInfo ci) {
        if (type == LAccess.enabled && !fcForceDisable && !fcForceEnable) {
            enabled = !Mathf.zero((float) p1);
        }
    }

    @Inject(method = "update", at = @At("HEAD"), remap = false)
    public void fcUpdate(CallbackInfo ci) {
        if (fcForceDisable || fcForceEnable) enabled = fcForceEnable;
        if (power != null) if (Float.isNaN(power.status)) if (fcInfinityPower) {
            fcInfinityPower = false;
            if (health == Float.POSITIVE_INFINITY) health = maxHealth;
            Time.run(120f, this::kill);
        }
    }

    @Inject(method = "display", at = @At("HEAD"), remap = false)
    private void fcDisplayBefore(Table table, CallbackInfo ci) {
        displayInfoEvent.form((Building) (Object) this, table, true);
        Events.fire(displayInfoEvent);

    }

    @Inject(method = "display", at = @At("RETURN"), remap = false)
    private void fcDisplayAfter(Table table, CallbackInfo ci) {
        displayInfoEvent.form((Building) (Object) this, table, false);
        Events.fire(displayInfoEvent);
    }
}
