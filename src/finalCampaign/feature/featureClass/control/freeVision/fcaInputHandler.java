package finalCampaign.feature.featureClass.control.freeVision;

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

public class fcaInputHandler extends InputHandler {

    protected float playerSelectRange = mobile ? 17f : 11f;

    protected boolean checkConfigTap(){
        return config.isShown() && config.getSelected().onConfigureTapped(input.mouseWorldX(), input.mouseWorldY());
    }

    /** Handles tile tap events that are not platform specific. */
    protected boolean tileTapped(@Nullable Building build){
        if(build == null){
            inv.hide();
            config.hideConfig();
            commandBuildings.clear();
            return false;
        }
        boolean consumed = false, showedInventory = false;

        //select building for commanding
        if(build.block.commandable && commandMode){
            //TODO handled in tap.
            consumed = true;
        }else if(build.block.configurable && build.interactable(player.team())){ //check if tapped block is configurable
            consumed = true;
            if((!config.isShown() && build.shouldShowConfigure(player)) //if the config fragment is hidden, show
            //alternatively, the current selected block can 'agree' to switch config tiles
            || (config.isShown() && config.getSelected().onConfigureBuildTapped(build) && build.shouldShowConfigure(player))){
                Sounds.click.at(build);
                config.showConfig(build);
            }
            //otherwise...
        }else if(!config.hasConfigMouse()){ //make sure a configuration fragment isn't on the cursor
            //then, if it's shown and the current block 'agrees' to hide, hide it.
            if(config.isShown() && config.getSelected().onConfigureBuildTapped(build)){
                consumed = true;
                config.hideConfig();
            }

            if(config.isShown()){
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
                inv.showFor(build);
                consumed = true;
                showedInventory = true;
            }
        }

        if(!showedInventory){
            inv.hide();
        }

        return consumed;
    }

    /** Tries to select the player to drop off items, returns true if successful. */
    protected boolean tryTapPlayer(float x, float y){
        if(canTapPlayer(x, y)){
            droppingItem = true;
            return true;
        }
        return false;
    }

    protected boolean canTapPlayer(float x, float y){
        return player.within(x, y, playerSelectRange) && player.unit().stack.amount > 0;
    }

    /** Tries to begin mining a tile, returns true if successful. */
    protected boolean tryBeginMine(Tile tile){
        if(canMine(tile)){
            player.unit().mineTile = tile;
            return true;
        }
        return false;
    }

    /** Tries to stop mining, returns true if mining was stopped. */
    protected boolean tryStopMine(){
        if(player.unit().mining()){
            player.unit().mineTile = null;
            return true;
        }
        return false;
    }

    protected boolean tryStopMine(Tile tile){
        if(player.unit().mineTile == tile){
            player.unit().mineTile = null;
            return true;
        }
        return false;
    }

    protected boolean tryRepairDerelict(Tile selected){
        if(selected != null && player.team() != Team.derelict && selected.build != null && selected.build.block.unlockedNow() && selected.build.team == Team.derelict && Build.validPlace(selected.block(), player.team(), selected.build.tileX(), selected.build.tileY(), selected.build.rotation)){
            player.unit().addBuild(new BuildPlan(selected.build.tileX(), selected.build.tileY(), selected.build.rotation, selected.block(), selected.build.config()));
            return true;
        }
        return false;
    }

    protected boolean canMine(Tile tile){
        return !Core.scene.hasMouse()
            && player.unit().validMine(tile)
            && player.unit().acceptsItem(player.unit().getMineResult(tile))
            && !((!Core.settings.getBool("doubletapmine") && tile.floor().playerUnmineable) && tile.overlay().itemDrop == null);
    }

    /** Returns the tile at the specified MOUSE coordinates. */
    protected Tile tileAt(float x, float y){
        return world.tile(tileX(x), tileY(y));
    }

    protected int rawTileX(){
        return World.toTile(Core.input.mouseWorld().x);
    }

    protected int rawTileY(){
        return World.toTile(Core.input.mouseWorld().y);
    }

    protected int tileX(float cursorX){
        Vec2 vec = Core.input.mouseWorld(cursorX, 0);
        if(selectedBlock()){
            vec.sub(block.offset, block.offset);
        }
        return World.toTile(vec.x);
    }

    protected int tileY(float cursorY){
        Vec2 vec = Core.input.mouseWorld(0, cursorY);
        if(selectedBlock()){
            vec.sub(block.offset, block.offset);
        }
        return World.toTile(vec.y);
    }
}
