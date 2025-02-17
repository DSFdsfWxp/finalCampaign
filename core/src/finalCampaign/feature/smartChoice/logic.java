package finalCampaign.feature.smartChoice;

import arc.*;
import arc.struct.*;
import finalCampaign.event.*;
import finalCampaign.input.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.heat.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.sandbox.*;
import mindustry.world.blocks.units.*;

import static mindustry.Vars.player;

public class logic {

    public static void update(fcInputHandleUpdateEvent e) {
        if (e.beforeUpdate || !fSmartChoice.enabled)
            return;

        if (!Core.scene.hasDialog() && !Core.scene.hasMouse() && !Core.scene.hasKeyboard()) {
            if (Core.input.keyDown(fcBindings.smartChoiceRoulette))
                ui.showRoulette(giveSuggestions());

            if (Core.input.keyRelease(fcBindings.smartChoiceRoulette))
                ui.closeRoulette();
        }
    }

    private static Seq<Block> giveSuggestions() {
        Seq<Block> res = new Seq<>();
        Seq<Block> tmp = new Seq<>();

        float worldX = Core.input.mouseWorldX();
        float worldY = Core.input.mouseWorldY();

        Tile tile = Vars.world.tileWorld(worldX, worldY);
        Building building = tile == null ? null : tile.build;
        Block block = tile == null ? null : tile.block();
        Floor overlay = tile == null ? null : tile.overlay();
        Floor floor = tile == null ? null : tile.floor();
        Unit unit = Units.closestOverlap(player.team(), worldX, worldY, 5f, u -> !u.isLocal());

        if (overlay instanceof OreBlock ore) {
            tmp.add(Vars.content.blocks().select(b -> {
                if (invalidBlock(b))
                    return false;

                if (!(b instanceof Drill) && !(b instanceof BeamDrill))
                    return false;

                if ((ore.wallOre && !(b instanceof BeamDrill)) ||
                    (!ore.wallOre && !(b instanceof Drill)))
                        return false;

                int tier = 0;
                if (b instanceof Drill db)
                    tier = db.tier;
                if (b instanceof BeamDrill bdb)
                    tier = bdb.tier;

                return ore.itemDrop.hardness <= tier;
            }));

            tmp.sort(b -> {
                if (b instanceof Drill db)
                    return db.tier;
                if (b instanceof BeamDrill bdb)
                    return bdb.tier;
                return 0f;
            });

            res.add(tmp);
            tmp.clear();
        }

        if (floor != null) {
            res.add(Vars.content.blocks().select(b -> {
                if (invalidBlock(b))
                    return false;

                if (b instanceof ThermalGenerator tgb)
                    return floor.attributes.get(tgb.attribute) != 0;

                if (b instanceof AttributeCrafter acb)
                    return floor.attributes.get(acb.attribute) != 0;

                if (b instanceof SolidPump spb)
                    return floor.attributes.get(spb.attribute) != 0;

                if (b instanceof Pump)
                    return floor.isLiquid;

                if (b instanceof WallCrafter wcb)
                    return floor.solid && floor.attributes.get(wcb.attribute) != 0;

                if (b instanceof Drill db)
                    return floor.itemDrop != null && db.tier >= floor.itemDrop.hardness;

                return false;
            }));
        }

        if (building != null && block != null) {
            boolean consumeItem = false;
            boolean consumeLiquid = false;
            boolean acceptItem = false;
            boolean acceptLiquid = false;

            for (var i : Vars.content.items()) {
                if (block.consumesItem(i))
                    consumeItem = true;
                if (building.acceptItem(building, i))
                    acceptItem = true;
                if (consumeItem && acceptItem)
                    break;
            }

            for (var l : Vars.content.liquids()) {
                if (block.consumesLiquid(l))
                    consumeLiquid = true;
                if (building.acceptLiquid(building, l))
                    acceptLiquid = true;
                if (consumeLiquid && acceptLiquid)
                    break;
            }

            boolean finalConsumeItem = consumeItem;
            boolean finalConsumeLiquid = consumeLiquid;
            boolean finalAcceptItem = acceptItem;
            boolean finalAcceptLiquid = acceptLiquid;
            boolean consumeHeat = building instanceof HeatConsumer || (block instanceof Turret tb && tb.heatRequirement > 0f);

            tmp.add(Vars.content.blocks().select(b -> {
                if (invalidBlock(b))
                    return false;

                if (b instanceof PowerSource)
                    return block.consumesPower;

                if (b instanceof PowerNode)
                    return block.consumesPower;

                if (b instanceof Conveyor || b instanceof Duct || b instanceof DirectionalUnloader)
                    return finalConsumeItem;

                if (b instanceof Conduit || b instanceof SolidPump)
                    return finalConsumeLiquid;

                if (b instanceof MendProjector || b instanceof RegenProjector)
                    return building.healthf() < 1f;

                if (b instanceof HeatProducer || b instanceof HeaterGenerator || b instanceof HeatConductor)
                    return consumeHeat;

                return false;
            }));

            tmp.sort(b -> {
                if (b instanceof PowerNode)
                    return building.power.status < 1f ? 1f : -1f;

                if (b instanceof ArmoredConveyor)
                    return finalAcceptItem ? 0f : -1f;

                if (b instanceof ArmoredConduit)
                    return finalAcceptLiquid ? 0f : -1f;

                if (b instanceof Conveyor || b instanceof Duct || b instanceof DirectionalUnloader)
                    return finalAcceptItem ? 1f : -1f;

                if (b instanceof Conduit || b instanceof SolidPump)
                    return finalAcceptLiquid ? 1f : -1f;

                if (b instanceof MendProjector || b instanceof RegenProjector)
                    return building.healthf() < 0.5f ? 2f :
                            building.healthf() < 0.8f ? 1f : -1f;

                if (b instanceof HeatProducer || b instanceof HeaterGenerator || b instanceof HeatConductor)
                    return 0f;

                return -2f;
            });

            res.add(tmp);
            tmp.clear();
        }

        if (block instanceof Conveyor) {
            res.add(Vars.content.blocks().select(b -> {
                if (invalidBlock(b))
                    return false;

                if (b instanceof ArmoredConveyor)
                    return true;

                if (b instanceof OverflowGate)
                    return true;

                if (b instanceof ItemBridge)
                    return true;

                if (b instanceof Junction)
                    return true;

                if (b instanceof Router)
                    return true;

                if (b instanceof Sorter)
                    return true;

                if (b instanceof StackConveyor)
                    return true;

                return false;
            }));
        }

        if (block instanceof Duct) {
            res.add(Vars.content.blocks().select(b -> {
                if (invalidBlock(b))
                    return false;

                if (b instanceof DirectionBridge)
                    return true;

                if (b instanceof DuctRouter)
                    return true;

                if (b instanceof OverflowDuct)
                    return true;

                return false;
            }));
        }

        if (block instanceof Conduit) {
            res.add(Vars.content.blocks().select(b -> {
                if (invalidBlock(b))
                    return false;

                if (b instanceof ArmoredConduit)
                    return true;

                if (b instanceof LiquidBridge)
                    return true;

                if (b instanceof LiquidJunction)
                    return true;

                if (b instanceof LiquidRouter)
                    return true;

                if (b instanceof DirectionLiquidBridge)
                    return true;

                return false;
            }));
        }

        if (unit != null) {
            res.add(Vars.content.blocks().select(b -> {
                if (invalidBlock(b))
                    return false;

                if (b instanceof RepairTurret)
                    return true;

                if (b instanceof RepairTower)
                    return true;

                return false;
            }));
        }

        if (block == null && building == null && Vars.indexer.findTile(player.team(), worldX, worldY, 5f * Vars.tilesize, b -> true) == null) {
            res.add(Vars.content.blocks().select(b -> {
                if (invalidBlock(b))
                    return false;

                if (b instanceof PowerNode pnb)
                    return pnb.laserRange > 10f;

                if (b instanceof BeamNode bnb)
                    return bnb.range > 10;

                if (b instanceof MassDriver)
                    return true;

                return false;
            }));
        }

        return res;
    }

    private static boolean invalidBlock(Block block) {
        return !block.isPlaceable() || (!block.unlockedNow() && !((Vars.state == null || Vars.state.rules.infiniteResources) || Vars.state.rules.editor)) || !block.environmentBuildable() || !block.placeablePlayer;
    }
}
