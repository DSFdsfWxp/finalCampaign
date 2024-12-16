package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import finalCampaign.event.*;
import finalCampaign.feature.featureClass.control.freeVision.*;
import finalCampaign.feature.featureClass.control.setMode.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.type.*;
import mindustry.world.blocks.*;

@Mixin(DesktopInput.class)
public abstract class fcDesktopInput extends InputHandler {

    @Shadow(remap = false)
    public Vec2 movement;
    @Shadow(remap = false)
    public float panSpeed, panBoostSpeed;
    @Shadow(remap = false)
    public boolean shouldShoot, panning;

    public Teamc target;
    public Teamc lastTarget;
    public float crosshairScale;
    
    private fcDrawWorldTopEvent drawTopEvent = new fcDrawWorldTopEvent();


    @Inject(method = "drawTop", at = @At("HEAD"), remap = false)
    public void fcDrawTop(CallbackInfo ci) {
        Events.fire(drawTopEvent);
    }

    @Inject(method = "updateState", at = @At("RETURN"), remap = false)
    private void fcUpdateState(CallbackInfo ci) {
        if (Vars.state.isMenu()) {
            target = lastTarget = null;
            crosshairScale = 0f;
            Vars.player.shooting = false;
        }
    }

    @Override
    public void drawOverSelect() {
        if (!fFreeVision.isOn())
            return;

        if(target != null && !Vars.state.isEditor()){
            if(target != lastTarget){
                crosshairScale = 0f;
                lastTarget = target;
            }

            crosshairScale = Mathf.lerpDelta(crosshairScale, 1f, 0.2f);

            Drawf.target(target.getX(), target.getY(), 7f * Interp.swingIn.apply(crosshairScale), Pal.remove);
        }

        Draw.reset();
    }

    private void checkTargets(float x, float y) {
        Unit unit = Units.closestEnemy(Vars.player.team(), x, y, 20f, (u) -> !u.dead);

        if(unit != null && Vars.player.unit().type.canAttack){
            Vars.player.unit().mineTile = null;
            target = unit;
        }else{
            Building tile = Vars.world.buildWorld(x, y);

            if((tile != null && Vars.player.team() != tile.team && (tile.team != Team.derelict || Vars.state.rules.coreCapture) && !Vars.player.unit().type.canHeal) || (tile != null && Vars.player.unit().type.canHeal && tile.team == Vars.player.team() && tile.damaged())){
                Vars.player.unit().mineTile = null;
                target = tile;
            }
        }
    }

    @Inject(method = "tap", at = @At("HEAD"), remap = false)
    private void fcTap(float x, float y, int count, KeyCode button, CallbackInfoReturnable<Boolean> ci) {
        if (Core.scene.hasMouse() || !commandMode)
            return;

        float worldx = Core.input.mouseWorld(x, y).x, worldy = Core.input.mouseWorld(x, y).y;

        if (fFreeVision.isOn()) {
            if(!Vars.player.dead()) {
                checkTargets(worldx, worldy);
            }
        }
    }

    @Inject(method = "update", at = @At("RETURN"), remap = false)
    private void fcUpdate(CallbackInfo ci) {
        boolean locked = locked();
        Unit spectating = null;

        try {
            spectating = Reflect.get(InputHandler.class, this, "spectating");
        } catch (Throwable ignore) {}

        if (!locked && fFreeVision.isOn() && !panning) {
            if ((!Vars.player.dead() || spectating != null) && !Vars.state.isPaused() && !Vars.ui.chatfrag.shown() && !Core.scene.hasField() && !Core.scene.hasDialog() && !Vars.ui.consolefrag.shown()) {
                // un-lerpDelta of Vec2
                Team corePanTeam = Vars.state.won ? Vars.state.rules.waveTeam : Vars.player.team();
                Position coreTarget = Vars.state.gameOver && !Vars.state.rules.pvp && corePanTeam.data().lastCore != null ? corePanTeam.data().lastCore : null;
                Position panTarget = coreTarget != null ? coreTarget : (spectating != null ? spectating : Vars.player);
                float alpha = Mathf.clamp((Core.settings.getBool("smoothcamera") ? 0.08f : 1f) * Time.delta);
                float invAlpha = 1.0f - alpha;

                Core.camera.position.x -= panTarget.getX() * alpha;
                Core.camera.position.y -= panTarget.getY() * alpha;
                Core.camera.position.x /= invAlpha;
                Core.camera.position.y /= invAlpha;

                // pan camera
                float camSpeed = (!Core.input.keyDown(Binding.boost) ? panSpeed : panBoostSpeed) * Time.delta;
                Core.camera.position.add(Tmp.v1.setZero().add(Core.input.axis(Binding.move_x), Core.input.axis(Binding.move_y)).nor().scl(camSpeed));
            }
        }

        shouldShoot = !Core.scene.hasMouse() && !locked && !fSetMode.isOn();

        if (Core.input.keyRelease(Binding.select) || fSetMode.isOn()) {
            Vars.player.shooting = false;
        }
    }

    // code from MobileInputHandle, modified.
    @Inject(method = "updateMovement", at = @At("RETURN"), remap = false)
    private void fcUpdateMovement(Unit unit, CallbackInfo ci) {
        if (!fFreeVision.isOn())
            return;

        UnitType type = unit.type;
        if (type == null)
            return;
        
        Rect rect = Tmp.r3;
        
        boolean omni = unit.type.omniMovement;
        boolean boosted = (unit instanceof Mechc && unit.isFlying());
        boolean allowHealing = type.canHeal;
        boolean validHealTarget = allowHealing && target instanceof Building b && b.isValid() && target.team() == unit.team && b.damaged() && target.within(unit, type.range);

        final float attractDst = 15f;
        float range = unit.hasWeapons() ? unit.range() : 0f;
        float bulletSpeed = unit.hasWeapons() ? type.weapons.first().bullet.speed : 0f;
        float mouseAngle = unit.angleTo(unit.aimX(), unit.aimY());
        boolean aimCursor = omni && Vars.player.shooting && type.hasWeapons() && !boosted && type.faceTarget && !fSetMode.isOn();

        if((Units.invalidateTarget(target, unit, type.range) && !validHealTarget) || Vars.state.isEditor()){
            if (target != null) Vars.player.shooting = false;
            target = null;
        }

        if(aimCursor){
            unit.lookAt(mouseAngle);
        }else{
            unit.lookAt(unit.prefRotation());
        }

        // we don't care payload here
        // we're on desktop

        movement.set(Core.camera.position).sub(Vars.player).limit(unit.speed());
        movement.setAngle(Mathf.slerp(movement.angle(), unit.vel.angle(), 0.05f));

        if (Vars.player.within(Core.camera.position, attractDst)) {
            movement.setZero();
            unit.vel.approachDelta(Vec2.ZERO, unit.speed() * type.accel / 2f);
        }

        unit.hitbox(rect);
        rect.grow(4f);

        Vars.player.boosting = Vars.collisions.overlapsTile(rect, EntityCollisions::solid) || !unit.within(Core.camera.position, 85f);

        unit.movePref(movement);

        //update shooting if not building + not mining
        if(!Vars.player.unit().activelyBuilding() && Vars.player.unit().mineTile == null){

            //autofire targeting
            if (target == null) {
                //Vars.player.shooting = shouldShoot;
                if (!(Vars.player.unit() instanceof BlockUnitUnit u && u.tile() instanceof ControlBlock c && !c.shouldAutoTarget()) && fFreeVision.autoTargetingEnabled()) {
                    if (Vars.player.unit().type.canAttack) {
                        target = Units.closestTarget(unit.team, unit.x, unit.y, range, (u) -> u.checkTarget(type.targetAir, type.targetGround), (u) -> type.targetGround);
                    }

                    if (allowHealing && target == null) {
                        target = Geometry.findClosest(unit.x, unit.y, Vars.indexer.getDamaged(Vars.player.team()));
                        if (target != null && !unit.within(target, range)) {
                            //Vars.player.shooting = false;
                            target = null;
                        }
                    }
                }

                //when not shooting, aim at mouse cursor
                //this may be a bad idea, aiming for a point far in front could work better, test it out
                unit.aim(Core.input.mouseWorldX(), Core.input.mouseWorldY());
            } else {
                Vec2 intercept = Predict.intercept(unit, target, bulletSpeed);

                Vars.player.mouseX = intercept.x;
                Vars.player.mouseY = intercept.y;
                Vars.player.shooting = !boosted;

                unit.aim(Vars.player.mouseX, Vars.player.mouseY);
            }
        }

        unit.controlWeapons(true, Vars.player.shooting && !boosted);
    }
}
