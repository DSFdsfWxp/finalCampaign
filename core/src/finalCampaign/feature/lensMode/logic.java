package finalCampaign.feature.lensMode;

import arc.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import finalCampaign.feature.setMode.*;
import finalCampaign.input.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.type.*;
import mindustry.world.blocks.*;

public class logic {
    public static Teamc target;
    public static Teamc lastTarget;
    public static float crosshairScale;

    private static boolean keyHookEnabled = false;
    private static boolean keyHookBanScroll = false;
    private static DesktopInput input;
    private static Vec2 lastCameraPos = new Vec2();

    public static void updateState() {
        if (Vars.control.input instanceof DesktopInput di)
            input = di;
        else
            input = null;

        if (Vars.state.isMenu()) {
            target = lastTarget = null;
            crosshairScale = 0f;
            Vars.player.shooting = false;
            keyHookEnabled = keyHookBanScroll = false;
            lastCameraPos.setZero();
        }
    }

    public static void drawOverSelect() {
        if (input == null || !fLensMode.autoTargeting)
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

    private static void checkTargets(float x, float y) {
        Unit unit = Units.closestEnemy(Vars.player.team(), x, y, 20f, (u) -> !u.dead);

        if (unit != null && Vars.player.unit().type.canAttack) {
            Vars.player.unit().mineTile = null;
            target = unit;
        } else {
            Building tile = Vars.world.buildWorld(x, y);

            if((tile != null && Vars.player.team() != tile.team && (tile.team != Team.derelict || Vars.state.rules.coreCapture) && !Vars.player.unit().type.canHeal) || (tile != null && Vars.player.unit().type.canHeal && tile.team == Vars.player.team() && tile.damaged())){
                Vars.player.unit().mineTile = null;
                target = tile;
            }
        }
    }

    public static void tap(float x, float y, int count, KeyCode button) {
        if (Core.scene.hasMouse() || !input.commandMode)
            return;

        float worldx = Core.input.mouseWorld(x, y).x, worldy = Core.input.mouseWorld(x, y).y;

        if (fLensMode.enabled && input != null && fLensMode.autoTargeting) {
            if(!Vars.player.dead()) {
                checkTargets(worldx, worldy);
            }
        }
    }

    public static void updateBefore() {
        if (fLensMode.enabled && input != null) {
            keyHookEnabled = true;

            if (fLensMode.mode != fLensMode.lensMode.defaultCamera) {
                if (Core.input.keyDown(fcBindings.boostCamera) && Core.input.keyDown(fcBindings.slowCamera)) {
                    keyHookBanScroll = true;
                    ui.makeSureSpeedSetToastOpen();
                    float delta = fcInputHook.getRealAxis(KeyCode.scroll) * 0.1f;
                    fLensMode.setPanSpeedPercent(Mathf.clamp(fLensMode.panSpeedPercent + delta, input.panSpeed * 0.4f, input.panBoostSpeed));
                } else {
                    keyHookBanScroll = false;
                    ui.closeSpeedSetToast();
                }
            }
        }
    }

    public static void updateAfter() {
        if (!fLensMode.enabled || input == null)
            return;

        boolean locked = input.locked();
        Unit spectating = null;

        try {
            spectating = Reflect.get(InputHandler.class, input, "spectating");
        } catch (Throwable ignore) {}

        // detach camera
        if (!locked && fLensMode.mode != fLensMode.lensMode.defaultCamera && !input.panning) {
            if (spectating == null && !Vars.ui.chatfrag.shown() && !Core.scene.hasField() && !Core.scene.hasDialog() && !Vars.ui.consolefrag.shown()) {
                // pan camera
                float camSpeed = (
                        !fcInputHook.realIsPressed(fcBindings.boostCamera) ?
                                (fcInputHook.realIsPressed(fcBindings.slowCamera) ? input.panSpeed * 0.4f : input.panBoostSpeed) :
                                input.panSpeed * fLensMode.panSpeedPercent
                ) * Time.delta;
                Core.camera.position.add(Tmp.v1.setZero().add(fcInputHook.getRealAxis(Binding.move_x), fcInputHook.getRealAxis(Binding.move_y)).nor().scl(camSpeed));
            }
        }

        input.shouldShoot = !Core.scene.hasMouse() && !locked && !fSetMode.isOn();

        if (Core.input.keyRelease(Binding.select) || fSetMode.isOn()) {
            Vars.player.shooting = false;
        }

        keyHookEnabled = false;
    }

    public static void updateMovementBefore(Unit unit) {
        if (!fLensMode.enabled)
            return;

        if (fLensMode.mode == fLensMode.lensMode.freeCamera) {
            if (input == null) {
                lastCameraPos.set(Core.camera.position);
                Core.camera.position.set(unit);
            } else {
                keyHookEnabled = true;
            }
        }

        if (fLensMode.mode == fLensMode.lensMode.followCamera)
            if (input != null)
                keyHookEnabled = true;
    }

    public static void updateMovementAfter(Unit unit) {
        if (!fLensMode.enabled)
            return;

        // special mode on desktop
        if (input != null && fLensMode.mode != fLensMode.lensMode.defaultCamera) {
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
            boolean aimCursor = omni && Vars.player.shooting && type.hasWeapons() && !boosted && type.faceTarget && !fSetMode.isOn() && target == null;

            if ((Units.invalidateTarget(target, unit, type.range) && !validHealTarget) || Vars.state.isEditor()) {
                if (target != null) Vars.player.shooting = false;
                target = null;
            }

            if (aimCursor) {
                unit.lookAt(mouseAngle);
            } else {
                unit.lookAt(unit.prefRotation());
            }

            // we don't care payload here
            // we're on desktop

            // unit follow camera
            if (fLensMode.mode == fLensMode.lensMode.followCamera) {
                input.movement.set(Core.camera.position).sub(Vars.player).limit(unit.speed());
                input.movement.setAngle(Mathf.slerp(input.movement.angle(), unit.vel.angle(), 0.05f));

                if (Vars.player.within(Core.camera.position, attractDst)) {
                    input.movement.setZero();
                    unit.vel.approachDelta(Vec2.ZERO, unit.speed() * type.accel / 2f);
                }

                unit.hitbox(rect);
                rect.grow(4f);

                Vars.player.boosting = Vars.collisions.overlapsTile(rect, EntityCollisions::solid) || !unit.within(Core.camera.position, 85f);

                unit.movePref(input.movement);
            }

            // auto targeting
            // update shooting if not building + not mining
            if (!Vars.player.unit().activelyBuilding() && Vars.player.unit().mineTile == null && fLensMode.autoTargeting) {

                // autofire targeting
                if (target == null) {
                    //Vars.player.shooting = shouldShoot;
                    if (!(Vars.player.unit() instanceof BlockUnitUnit u && u.tile() instanceof ControlBlock c && !c.shouldAutoTarget())) {
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

                    // when not shooting, aim at mouse cursor
                    // this may be a bad idea, aiming for a point far in front could work better, test it out
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

        if (fLensMode.mode == fLensMode.lensMode.freeCamera) {
            if (input == null) {
                Core.camera.position.set(lastCameraPos);
            } else {
                keyHookEnabled = false;
            }
        }

        if (fLensMode.mode == fLensMode.lensMode.followCamera)
            if (input != null)
                keyHookEnabled = false;
    }

    protected static float keyHook(KeyCode code, float v) {
        if (!keyHookEnabled || !fLensMode.enabled || fLensMode.mode != fLensMode.lensMode.defaultCamera)
            return v;

        if (code == KeyCode.scroll && keyHookBanScroll)
            return 0f;

        return v;
    }

    protected static boolean keyHook(KeyCode code, boolean v) {
        if (!keyHookEnabled || !fLensMode.enabled || fLensMode.mode != fLensMode.lensMode.defaultCamera)
            return v;

        var moveX = Core.keybinds.get(Binding.move_x);
        var moveY = Core.keybinds.get(Binding.move_y);

        if (code == moveX.min || code == moveX.max ||
                code == moveY.min || code == moveY.max)
            return false;

        if (code == Core.keybinds.get(Binding.mouse_move).key)
            return false;

        return v;
    }
}
