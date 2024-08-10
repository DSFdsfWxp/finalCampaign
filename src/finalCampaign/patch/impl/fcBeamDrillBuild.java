package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import finalCampaign.patch.*;
import mindustry.*;
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

    private BeamDrill fcDrill = (BeamDrill) this.block;
    private Item fcPreferItem;
    private ObjectIntMap<Item> fcOutputMap = new ObjectIntMap<>();

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
        float multiplier = Mathf.lerp(1f, fcDrill.optionalBoostIntensity, optionalEfficiency);
        float drillTime = fcDrill.getDrillTime(lastItem);
        return (amount * multiplier * timeScale) / drillTime;
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
    public void fcOnProximityUpdate() {
        fcCheckItem();
    }

    @Inject(method = "read", at = @At("RETURN"), remap = false)
    public void fcRead(Reads read, byte revision) {
        fcPreferItem = TypeIO.readItem(read);
    }

    @Inject(method = "write", at = @At("RETURN"), remap = false)
    public void fcWrite(Writes write) {
        TypeIO.writeItem(write, fcPreferItem);
    }
}
