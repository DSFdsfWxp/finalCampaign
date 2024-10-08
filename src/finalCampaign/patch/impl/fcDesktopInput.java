package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.*;
import arc.Graphics.*;
import arc.Graphics.Cursor.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import finalCampaign.event.*;
import finalCampaign.feature.featureClass.control.freeVision.*;
import finalCampaign.feature.featureClass.control.setMode.*;
import mindustry.core.World;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static mindustry.input.PlaceMode.*;

@Mixin(DesktopInput.class)
public abstract class fcDesktopInput extends InputHandler {

    @Shadow(remap = false)
    public Vec2 movement;
    @Shadow(remap = false)
    public Cursor cursorType;
    @Shadow(remap = false)
    public int selectX, selectY, schemX, schemY;
    @Shadow(remap = false)
    public int lastLineX, lastLineY, schematicX, schematicY;
    @Shadow(remap = false)
    public PlaceMode mode;
    @Shadow(remap = false)
    public float selectScale;
    @Shadow(remap = false)
    public @Nullable BuildPlan splan;

    @Shadow(remap = false)
    public float panScale, panSpeed, panBoostSpeed;

    @Shadow(remap = false)
    public boolean deleting, shouldShoot, panning;

    @Shadow(remap = false)
    public Tile prevSelected;
    @Shadow(remap = false)
    public long selectMillis;

    public boolean manualShooting = false;

    public Teamc target;
    public Teamc lastTarget;
    public Vec2 targetPos = new Vec2();
    public float crosshairScale;
    
    private fcDrawWorldTopEvent drawTopEvent = new fcDrawWorldTopEvent();

    int maxLength = 100;

    @Shadow(remap = false)
    abstract void pollInput();

    @Inject(method = "drawTop", at = @At("HEAD"), remap = false)
    public void fcDrawTopInject(CallbackInfo ci) {
        Events.fire(drawTopEvent);
    }

    @Override
    public void updateState(){
        super.updateState();

        if(state.isMenu()){
            lastSchematic = null;
            droppingItem = false;
            mode = none;
            block = null;
            splan = null;
            selectPlans.clear();
            manualShooting = false;
        }
    }

    @Override
    public void drawOverSelect() {
        if (!fFreeVision.isOn()) {
            super.drawOverSelect();
            return;
        }

        if(target != null && !state.isEditor() && !manualShooting){
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
        Unit unit = Units.closestEnemy(player.team(), x, y, 20f, (u) -> !u.dead);

        if(unit != null && player.unit().type.canAttack){
            player.unit().mineTile = null;
            target = unit;
        }else{
            Building tile = world.buildWorld(x, y);

            if((tile != null && player.team() != tile.team && (tile.team != Team.derelict || state.rules.coreCapture) && !player.unit().type.canHeal) || (tile != null && player.unit().type.canHeal && tile.team == player.team() && tile.damaged())){
                player.unit().mineTile = null;
                target = tile;
            }
        }
    }

    @Override
    public boolean tap(float x, float y, int count, KeyCode button){
        float worldx = Core.input.mouseWorld(x, y).x, worldy = Core.input.mouseWorld(x, y).y;

        if (fFreeVision.isOn()) {
            if(!player.dead()){
                checkTargets(worldx, worldy);
            }
        }

        if(scene.hasMouse() || !commandMode) return false;

        tappedOne = true;

        //click: select a single unit
        if(button == KeyCode.mouseLeft){
            if(count >= 2){
                selectTypedUnits();
            }else{
                tapCommandUnit();
            }

        }

        return super.tap(x, y, count, button);
    }

    @Override
    public void update(){
        super.update();

        if(net.active() && Core.input.keyTap(Binding.player_list) && (scene.getKeyboardFocus() == null || scene.getKeyboardFocus().isDescendantOf(ui.listfrag.content) || scene.getKeyboardFocus().isDescendantOf(ui.minimapfrag.elem))){
            ui.listfrag.toggle();
        }

        boolean locked = locked();
        boolean panCam = false;
        float camSpeed = (!Core.input.keyDown(Binding.boost) ? panSpeed : panBoostSpeed) * Time.delta;

        if(input.keyDown(Binding.pan) && !scene.hasField() && !scene.hasDialog()){
            panCam = true;
            panning = true;
        }

        if((Math.abs(Core.input.axis(Binding.move_x)) > 0 || Math.abs(Core.input.axis(Binding.move_y)) > 0 || input.keyDown(Binding.mouse_move)) && (!scene.hasField())){
            panning = false;
        }

        if(!locked){
            if(((player.dead() || state.isPaused()) && !ui.chatfrag.shown()) && !scene.hasField() && !scene.hasDialog()){
                if(input.keyDown(Binding.mouse_move)){
                    panCam = true;
                }

                Core.camera.position.add(Tmp.v1.setZero().add(Core.input.axis(Binding.move_x), Core.input.axis(Binding.move_y)).nor().scl(camSpeed));
            }else if(!player.dead() && !panning){
                Team corePanTeam = state.won ? state.rules.waveTeam : player.team();
                Position coreTarget = state.gameOver && !state.rules.pvp && corePanTeam.data().lastCore != null ? corePanTeam.data().lastCore : null;
                
                if (!fFreeVision.isOn()) {
                    Core.camera.position.lerpDelta(coreTarget != null ? coreTarget : player, Core.settings.getBool("smoothcamera") ? 0.08f : 1f);
                } else {
                    if (!ui.consolefrag.shown() && !ui.chatfrag.shown() && !scene.hasField() && !scene.hasDialog())
                        Core.camera.position.add(Tmp.v1.setZero().add(Core.input.axis(Binding.move_x), Core.input.axis(Binding.move_y)).nor().scl(camSpeed));
                }
            }

            if(panCam){
                Core.camera.position.x += Mathf.clamp((Core.input.mouseX() - Core.graphics.getWidth() / 2f) * panScale, -1, 1) * camSpeed;
                Core.camera.position.y += Mathf.clamp((Core.input.mouseY() - Core.graphics.getHeight() / 2f) * panScale, -1, 1) * camSpeed;
            }
        }

        shouldShoot = !scene.hasMouse() && !locked && !fSetMode.isOn();

        if(!locked && block == null && !scene.hasField() && !scene.hasDialog() &&
                //disable command mode when player unit can boost and command mode binding is the same
                !(!player.dead() && player.unit().type.canBoost && keybinds.get(Binding.command_mode).key == keybinds.get(Binding.boost).key)){
            if(settings.getBool("commandmodehold")){
                commandMode = input.keyDown(Binding.command_mode);
            }else if(input.keyTap(Binding.command_mode)){
                commandMode = !commandMode;
            }
        }else{
            commandMode = false;
        }

        //validate commanding units
        selectedUnits.removeAll(u -> !u.isCommandable() || !u.isValid());

        if(commandMode && input.keyTap(Binding.select_all_units) && !scene.hasField() && !scene.hasDialog()){
            selectedUnits.clear();
            commandBuildings.clear();
            for(var unit : player.team().data().units){
                if(unit.isCommandable()){
                    selectedUnits.add(unit);
                }
            }
        }

        if(commandMode && input.keyTap(Binding.select_all_unit_factories) && !scene.hasField() && !scene.hasDialog()){
            selectedUnits.clear();
            commandBuildings.clear();
            for(var build : player.team().data().buildings){
                if(build.block.commandable){
                    commandBuildings.add(build);
                }
            }
        }

        if(!scene.hasMouse() && !locked && state.rules.possessionAllowed){
            if(Core.input.keyDown(Binding.control) && Core.input.keyTap(Binding.select)){
                Unit on = selectedUnit();
                var build = selectedControlBuild();
                if(on != null){
                    Call.unitControl(player, on);
                    shouldShoot = false;
                    recentRespawnTimer = 1f;
                }else if(build != null){
                    Call.buildingControlSelect(player, build);
                    recentRespawnTimer = 1f;
                }
            }
        }

        if(!player.dead() && !state.isPaused() && !scene.hasField() && !locked){
            updateMovement(player.unit());

            if(Core.input.keyTap(Binding.respawn)){
                controlledType = null;
                recentRespawnTimer = 1f;
                Call.unitClear(player);
            }
        }

        if(Core.input.keyRelease(Binding.select)){
            player.shooting = false;
            manualShooting = false;
        }

        if(state.isGame() && !scene.hasDialog() && !scene.hasField()){
            if(Core.input.keyTap(Binding.minimap)) ui.minimapfrag.toggle();
            if(Core.input.keyTap(Binding.planet_map) && state.isCampaign()) ui.planet.toggle();
            if(Core.input.keyTap(Binding.research) && state.isCampaign()) ui.research.toggle();
        }

        if(state.isMenu() || Core.scene.hasDialog()) return;

        //zoom camera
        if((!Core.scene.hasScroll() || Core.input.keyDown(Binding.diagonal_placement)) && !ui.chatfrag.shown() && !ui.consolefrag.shown() && Math.abs(Core.input.axisTap(Binding.zoom)) > 0
            && !Core.input.keyDown(Binding.rotateplaced) && (Core.input.keyDown(Binding.diagonal_placement) ||
                !keybinds.get(Binding.zoom).equals(keybinds.get(Binding.rotate)) || ((!player.isBuilder() || !isPlacing() || !block.rotate) && selectPlans.isEmpty()))){
            renderer.scaleCamera(Core.input.axisTap(Binding.zoom));
        }

        if(Core.input.keyTap(Binding.select) && !Core.scene.hasMouse()){
            Tile selected = world.tileWorld(input.mouseWorldX(), input.mouseWorldY());
            if(selected != null){
                Call.tileTap(player, selected);
            }
        }

        if(player.dead() || locked){
            cursorType = SystemCursor.arrow;
            if(!Core.scene.hasMouse()){
                Core.graphics.cursor(cursorType);
            }
            return;
        }

        pollInput();

        //deselect if not placing
        if(!isPlacing() && mode == placing){
            mode = none;
        }

        if(player.shooting && !canShoot()){
            player.shooting = false;
            manualShooting = false;
        }

        if(isPlacing() && player.isBuilder()){
            cursorType = SystemCursor.hand;
            selectScale = Mathf.lerpDelta(selectScale, 1f, 0.2f);
        }else{
            selectScale = 0f;
        }

        if(!Core.input.keyDown(Binding.diagonal_placement) && Math.abs((int)Core.input.axisTap(Binding.rotate)) > 0){
            rotation = Mathf.mod(rotation + (int)Core.input.axisTap(Binding.rotate), 4);

            if(splan != null){
                splan.rotation = Mathf.mod(splan.rotation + (int)Core.input.axisTap(Binding.rotate), 4);
            }

            if(isPlacing() && mode == placing){
                updateLine(selectX, selectY);
            }else if(!selectPlans.isEmpty() && !ui.chatfrag.shown()){
                rotatePlans(selectPlans, Mathf.sign(Core.input.axisTap(Binding.rotate)));
            }
        }

        Tile cursor = world.tile(fcTileX(Core.input.mouseX()), fcTileY(Core.input.mouseY()));

        if(cursor != null){
            if(cursor.build != null){
                cursorType = cursor.build.getCursor();
            }

            if((isPlacing() && player.isBuilder()) || !selectPlans.isEmpty()){
                cursorType = SystemCursor.hand;
            }

            if(!isPlacing() && fcCanMine(cursor)){
                cursorType = ui.drillCursor;
            }

            if(commandMode && selectedUnits.any() && ((cursor.build != null && !cursor.build.inFogTo(player.team()) && cursor.build.team != player.team()) || (selectedEnemyUnit(input.mouseWorldX(), input.mouseWorldY()) != null))){
                cursorType = ui.targetCursor;
            }

            if(getPlan(cursor.x, cursor.y) != null && mode == none){
                cursorType = SystemCursor.hand;
            }

            if(fcCanTapPlayer(Core.input.mouseWorld().x, Core.input.mouseWorld().y)){
                cursorType = ui.unloadCursor;
            }


            if(cursor.build != null && cursor.interactable(player.team()) && !isPlacing() && Math.abs(Core.input.axisTap(Binding.rotate)) > 0 && Core.input.keyDown(Binding.rotateplaced) && cursor.block().rotate && cursor.block().quickRotate){
                Call.rotateBlock(player, cursor.build, Core.input.axisTap(Binding.rotate) > 0);
            }
        }

        if(!Core.scene.hasMouse()){
            Core.graphics.cursor(cursorType);
        }

        cursorType = SystemCursor.arrow;
    }

    protected void updateMovement(Unit unit){
        boolean omni = unit.type.omniMovement;

        float speed = unit.speed();
        float xa = Core.input.axis(Binding.move_x);
        float ya = Core.input.axis(Binding.move_y);
        boolean boosted = (unit instanceof Mechc && unit.isFlying());

        Rect rect = Tmp.r3;

        UnitType type = unit.type;

        boolean allowHealing = type.canHeal;
        boolean validHealTarget = allowHealing && target instanceof Building b && b.isValid() && target.team() == unit.team && b.damaged() && target.within(unit, type.range);

        float attractDst = 15f;

        float range = unit.hasWeapons() ? unit.range() : 0f;
        float bulletSpeed = unit.hasWeapons() ? type.weapons.first().bullet.speed : 0f;

        if (!fFreeVision.isOn()) {
            movement.set(xa, ya).nor().scl(speed);
            if(Core.input.keyDown(Binding.mouse_move)){
                movement.add(input.mouseWorld().sub(player).scl(1f / 25f * speed)).limit(speed);
            }
    
            float mouseAngle = Angles.mouseAngle(unit.x, unit.y);
            boolean aimCursor = omni && player.shooting && unit.type.hasWeapons() && unit.type.faceTarget && !boosted && !fSetMode.isOn();
    
            if(aimCursor){
                unit.lookAt(mouseAngle);
            }else{
                unit.lookAt(unit.prefRotation());
            }

            if (fSetMode.isOn()) player.shooting = false;
    
            unit.movePref(movement);
    
            unit.aim(Core.input.mouseWorld());
            unit.controlWeapons(true, player.shooting && !boosted);
    
            player.boosting = Core.input.keyDown(Binding.boost);
            player.mouseX = unit.aimX();
            player.mouseY = unit.aimY();
        } else {
            float mouseAngle = unit.angleTo(unit.aimX(), unit.aimY());
            boolean aimCursor = omni && player.shooting && type.hasWeapons() && !boosted && type.faceTarget && !fSetMode.isOn();

            if((Units.invalidateTarget(target, unit, type.range) && !validHealTarget) || state.isEditor()){
                if (target != null) player.shooting = false;
                target = null;
            }

            targetPos.set(Core.camera.position);

            if(aimCursor){
                unit.lookAt(mouseAngle);
            }else{
                unit.lookAt(unit.prefRotation());
            }
    
            movement.set(targetPos).sub(player).limit(speed);
            movement.setAngle(Mathf.slerp(movement.angle(), unit.vel.angle(), 0.05f));
    
            if(player.within(targetPos, attractDst)){
                movement.setZero();
                unit.vel.approachDelta(Vec2.ZERO, unit.speed() * type.accel / 2f);
            }
    
            unit.hitbox(rect);
            rect.grow(4f);
    
            player.boosting = collisions.overlapsTile(rect, EntityCollisions::solid) || !unit.within(targetPos, 85f);
    
            unit.movePref(movement);
    
            //update shooting if not building + not mining
            if(!player.unit().activelyBuilding() && player.unit().mineTile == null){
    
                //autofire targeting
                if(target == null){
                    //player.shooting = shouldShoot;
                    if(manualShooting){
                        player.shooting = !boosted;
                        unit.aim(player.mouseX = Core.input.mouseWorldX(), player.mouseY = Core.input.mouseWorldY());
                    }else if(!(player.unit() instanceof BlockUnitUnit u && u.tile() instanceof ControlBlock c && !c.shouldAutoTarget()) && fFreeVision.autoTargetingEnabled()){
                        if(player.unit().type.canAttack){
                            target = Units.closestTarget(unit.team, unit.x, unit.y, range, (u) -> u.checkTarget(type.targetAir, type.targetGround), (u) -> type.targetGround);
                        }
    
                        if(allowHealing && target == null){
                            target = Geometry.findClosest(unit.x, unit.y, indexer.getDamaged(player.team()));
                            if(target != null && !unit.within(target, range)){
                                //player.shooting = false;
                                target = null;
                            }
                        }
                    }
    
                    //when not shooting, aim at mouse cursor
                    //this may be a bad idea, aiming for a point far in front could work better, test it out
                    unit.aim(Core.input.mouseWorldX(), Core.input.mouseWorldY());
                }else{
                    Vec2 intercept = Predict.intercept(unit, target, bulletSpeed);
    
                    player.mouseX = intercept.x;
                    player.mouseY = intercept.y;
                    player.shooting = !boosted;
    
                    unit.aim(player.mouseX, player.mouseY);
                }
            }
    
            unit.controlWeapons(true, player.shooting && !boosted);

        }

        //update payload input
        if(unit instanceof Payloadc){
            if(Core.input.keyTap(Binding.pickupCargo)){
                tryPickupPayload();
            }

            if(Core.input.keyTap(Binding.dropCargo)){
                tryDropPayload();
            }
        }
    }

    private boolean fcCanMine(Tile tile){
        return !Core.scene.hasMouse()
            && player.unit().validMine(tile)
            && player.unit().acceptsItem(player.unit().getMineResult(tile))
            && !((!Core.settings.getBool("doubletapmine") && tile.floor().playerUnmineable) && tile.overlay().itemDrop == null);
    }

    private boolean fcCanTapPlayer(float x, float y){
        return player.within(x, y, mobile ? 17f : 11f) && player.unit().stack.amount > 0;
    }

    private int fcTileX(float cursorX){
        Vec2 vec = Core.input.mouseWorld(cursorX, 0);
        if(control.input.selectedBlock()){
            vec.sub(control.input.block.offset, control.input.block.offset);
        }
        return World.toTile(vec.x);
    }

    private int fcTileY(float cursorY){
        Vec2 vec = Core.input.mouseWorld(0, cursorY);
        if(control.input.selectedBlock()){
            vec.sub(control.input.block.offset, control.input.block.offset);
        }
        return World.toTile(vec.y);
    }

}
