package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import finalCampaign.map.fcMap;
import finalCampaign.patch.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.production.BeamDrill.*;

@Mixin(BeamDrillBuild.class)
public abstract class fcBeamDrillBuild extends Building implements IFcDrillBuild {
    @Shadow(remap = false)
    public @Nullable Item lastItem;
    @Shadow(remap = false)
    public Point2[] lasers;

    private BeamDrill fcDrill;
    private Item fcPreferItem;
    private ObjectIntMap<Item> fcOutputMap = new ObjectIntMap<>();

    @Override
    public Building create(Block block, Team team) {
        fcDrill = (BeamDrill) block;
        return super.create(block, team);
    }

    public void fcPreferItem(Item v) {
        fcPreferItem = v;
        fcCheckItem();
    }

    public Item fcDrillTarget() {
        return fcPreferItem == null ? lastItem : fcPreferItem;
    }

    public ObjectIntMap<Item> fcScanOutput() {
        fcScan();
        return new ObjectIntMap<>(fcOutputMap);
    }

    public float fcCalcDrillSpeed(Item item, int amount) {
        float speed = optionalEfficiency > 0f ? fcDrill.optionalBoostIntensity : 1f;
        return amount * speed * Math.max(1f, timeScale) / fcDrill.getDrillTime(item) * 3600f;
    }

    public void fcScan() {
        int dx = Geometry.d4x(rotation), dy = Geometry.d4y(rotation);
        fcOutputMap.clear();

        //update facing tiles
        for(int p = 0; p < fcDrill.size; p++){
            Point2 l = lasers[p];
            for(int i = 0; i < fcDrill.range; i++){
                int rx = l.x + dx*i, ry = l.y + dy*i;
                Tile other = Vars.world.tile(rx, ry);
                if(other != null){
                    if(other.solid()){
                        Item drop = other.wallDrop();
                        if(drop != null && drop.hardness <= fcDrill.tier){
                            fcOutputMap.increment(drop, 0, 1);
                        }
                        break;
                    }
                }
            }
        }
    }

    public void fcCheckItem() {
        if (fcPreferItem == null) return;
        fcScan();
        int amount = fcOutputMap.get(fcPreferItem, 0);
        if (amount > 0) lastItem = fcPreferItem;
    }

    @Inject(method = "onProximityUpdate", at = @At("RETURN"), remap = false)
    public void fcOnProximityUpdate(CallbackInfo ci) {
        fcCheckItem();
    }

    @Inject(method = "read", at = @At("RETURN"), remap = false)
    public void fcRead(Reads read, byte revision, CallbackInfo ci) {
        if (fcMap.currentVersion < 1) return;
        fcPreferItem = TypeIO.readItem(read);
    }

    @Inject(method = "write", at = @At("RETURN"), remap = false)
    public void fcWrite(Writes write, CallbackInfo ci) {
        if (fcMap.exportingPlainSave) return;
        TypeIO.writeItem(write, fcPreferItem);
    }
}
