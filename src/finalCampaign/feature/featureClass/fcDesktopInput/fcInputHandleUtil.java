package finalCampaign.feature.featureClass.fcDesktopInput;

import arc.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.core.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.input.*;
import mindustry.world.*;

import static arc.Core.*;
import static mindustry.Vars.*;

@SuppressWarnings("all")
public class fcInputHandleUtil {

    public static final float playerSelectRange = mobile ? 17f : 11f;

    public static boolean checkConfigTap(){
        return control.input.config.isShown() && control.input.config.getSelected().onConfigureTapped(input.mouseWorldX(), input.mouseWorldY());
    }

    /** Handles tile tap events that are not platform specific. */
    public static boolean tileTapped(@Nullable Building build){
        if(build == null){
            control.input.inv.hide();
            control.input.config.hideConfig();
            control.input.commandBuildings.clear();
            return false;
        }
        boolean consumed = false, showedInventory = false;

        //select building for commanding
        if(build.block.commandable && control.input.commandMode){
            consumed = true;
        }else if(build.block.configurable && build.interactable(player.team())){ //check if tapped block is configurable
            consumed = true;
            if((!control.input.config.isShown() && build.shouldShowConfigure(player)) //if the config fragment is hidden, show
            //alternatively, the current selected block can 'agree' to switch config tiles
            || (control.input.config.isShown() && control.input.config.getSelected().onConfigureBuildTapped(build) && build.shouldShowConfigure(player))){
                Sounds.click.at(build);
                control.input.config.showConfig(build);
            }
            //otherwise...
        }else if(!control.input.config.hasConfigMouse()){ //make sure a configuration fragment isn't on the cursor
            //then, if it's shown and the current block 'agrees' to hide, hide it.
            if(control.input.config.isShown() && control.input.config.getSelected().onConfigureBuildTapped(build)){
                consumed = true;
                control.input.config.hideConfig();
            }

            if(control.input.config.isShown()){
                consumed = true;
            }
        }

        //call tapped event
        if(!consumed && build.interactable(player.team())){
            build.tapped();
        }

        //consume tap event if necessary
        if(build.interactable(player.team()) && build.block.consumesTap){
            consumed = true;
        }else if(build.interactable(player.team()) && build.block.synthetic() && (!consumed || build.block.allowConfigInventory)){
            if(build.block.hasItems && build.items.total() > 0){
                control.input.inv.showFor(build);
                consumed = true;
                showedInventory = true;
            }
        }

        if(!showedInventory){
            control.input.inv.hide();
        }

        return consumed;
    }

    /** Tries to select the player to drop off items, returns true if successful. */
    public static boolean tryTapPlayer(float x, float y){
        if(canTapPlayer(x, y)){
            control.input.droppingItem = true;
            return true;
        }
        return false;
    }

    public static boolean canTapPlayer(float x, float y){
        return player.within(x, y, playerSelectRange) && player.unit().stack.amount > 0;
    }

    /** Tries to begin mining a tile, returns true if successful. */
    public static boolean tryBeginMine(Tile tile){
        if(canMine(tile)){
            player.unit().mineTile = tile;
            return true;
        }
        return false;
    }

    /** Tries to stop mining, returns true if mining was stopped. */
    public static boolean tryStopMine(){
        if(player.unit().mining()){
            player.unit().mineTile = null;
            return true;
        }
        return false;
    }

    public static boolean tryStopMine(Tile tile){
        if(player.unit().mineTile == tile){
            player.unit().mineTile = null;
            return true;
        }
        return false;
    }

    public static boolean tryRepairDerelict(Tile selected){
        if(selected != null && player.team() != Team.derelict && selected.build != null && selected.build.block.unlockedNow() && selected.build.team == Team.derelict && Build.validPlace(selected.block(), player.team(), selected.build.tileX(), selected.build.tileY(), selected.build.rotation)){
            player.unit().addBuild(new BuildPlan(selected.build.tileX(), selected.build.tileY(), selected.build.rotation, selected.block(), selected.build.config()));
            return true;
        }
        return false;
    }

    public static boolean canMine(Tile tile){
        return !Core.scene.hasMouse()
            && player.unit().validMine(tile)
            && player.unit().acceptsItem(player.unit().getMineResult(tile))
            && !((!Core.settings.getBool("doubletapmine") && tile.floor().playerUnmineable) && tile.overlay().itemDrop == null);
    }

    /** Returns the tile at the specified MOUSE coordinates. */
    public static Tile tileAt(float x, float y){
        return world.tile(tileX(x), tileY(y));
    }

    public static int rawTileX(){
        return World.toTile(Core.input.mouseWorld().x);
    }

    public static int rawTileY(){
        return World.toTile(Core.input.mouseWorld().y);
    }

    public static int tileX(float cursorX){
        Vec2 vec = Core.input.mouseWorld(cursorX, 0);
        if(control.input.selectedBlock()){
            vec.sub(control.input.block.offset, control.input.block.offset);
        }
        return World.toTile(vec.x);
    }

    public static int tileY(float cursorY){
        Vec2 vec = Core.input.mouseWorld(0, cursorY);
        if(control.input.selectedBlock()){
            vec.sub(control.input.block.offset, control.input.block.offset);
        }
        return World.toTile(vec.y);
    }
}
