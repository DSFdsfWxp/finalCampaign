package finalCampaign.patch.impl;

import java.lang.reflect.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import finalCampaign.patch.*;
import finalCampaign.util.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.production.Drill.*;

@Mixin(DrillBuild.class)
public abstract class fcDrillBuild extends Building implements IFcDrillBuild {
    @Shadow(remap = false)
    public int dominantItems;
    @Shadow(remap = false)
    public Item dominantItem;

    private Item fcPreferItem;
    private Drill fcDrill;
    private boolean fcInited = false;
    private ObjectIntMap<Item> fcOreCount;
    private Method fcCountOre;

    @Override
    public Building create(Block block, Team team) {
        fcDrill = (Drill) block;
        return super.create(block, team);
    }

    public void fcPreferItem(Item v) {
        fcPreferItem = v;
        fcCheckItem();
    }

    public ObjectIntMap<Item> fcScanOutput() {
        reflect.invoke(fcCountOre, fcDrill, tile);
        return new ObjectIntMap<>(fcOreCount);
    }

    public Item fcDrillTarget() {
        return fcPreferItem == null ? dominantItem : fcPreferItem;
    }

    public float fcCalcDrillSpeed(Item item, int amount) {
        float speed = optionalEfficiency > 0f ? fcDrill.liquidBoostIntensity : 1f;
        return speed * speed * amount / fcDrill.getDrillTime(item) * Math.max(1f, timeScale) * 3600f;
    }

    private void fcInit() {
        if (fcInited) return;
        fcOreCount = Reflect.get(Drill.class, fcDrill, "oreCount");
        fcCountOre = reflect.getDeclaredMethod(Drill.class, "countOre", Tile.class);
        reflect.setAccessible(fcCountOre, true);
        fcInited = true;
    }

    public void fcCheckItem() {
        if (!fcInited) fcInit();
        if (fcPreferItem == null) return;
        reflect.invoke(fcCountOre, fcDrill, tile);
        
        int amount = fcOreCount.get(fcPreferItem, 0);

        if (amount > 0) {
            dominantItem = fcPreferItem;
            dominantItems = amount;
        }
    }

    @Inject(method = "onProximityUpdate", at = @At("RETURN"), remap = false)
    private void fcOnProximityUpdate(CallbackInfo ci) {
        fcCheckItem();
    }

    @Inject(method = "read", at = @At("RETURN"), remap = false)
    private void fcRead(Reads read, byte revision, CallbackInfo ci) {
        if (finalCampaign.map.fcMap.currentVersion < 1) return;
        fcPreferItem = TypeIO.readItem(read);
    }

    @Inject(method = "write", at = @At("RETURN"), remap = false)
    private void fcWrite(Writes write, CallbackInfo ci) {
        TypeIO.writeItem(write, fcPreferItem);
    }
}
