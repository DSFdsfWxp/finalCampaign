package finalCampaign.feature.barDetail;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import finalCampaign.*;
import finalCampaign.graphics.*;
import finalCampaign.patch.*;
import finalCampaign.ui.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.storage.CoreBlock.*;
import mindustry.world.meta.*;

public class barDetail {
    public static <T extends Building> Func<T, Bar> transformBlock(String name, Block block) {
        if (name.equals("health")) {
            return b -> new fakeBar(new detailBar(b.maxHealth, 
                                                  () -> b.health, 
                                                  outlineIcons.health, 
                                                  Core.bundle.get("stat.health"), 
                                                  Pal.health)).blink(Color.white);
        } else if (name.startsWith("liquid-")) {
            Liquid liquid = Vars.content.liquid(name.substring(7));
            return b -> new fakeBar(new detailBar(b.block.liquidCapacity, 
                                                  () -> b.liquids.get(liquid), 
                                                  outlineIcons.findDrawable(liquid), 
                                                  liquid.localizedName, 
                                                  liquid.color));
        } else if (name.equals("items")) {
            return b -> new fakeBar(new detailBar(b.block.itemCapacity, 
                                                  () -> (float)b.items.total(), 
                                                  outlineIcons.totalItem, 
                                                  bundle.get("bar.totalItem"), 
                                                  Pal.items));
        } else if (name.equals("drillspeed")) {
            if (block instanceof Drill || block instanceof BeamDrill) {
                return b -> {
                    IFcDrillBuild db = (IFcDrillBuild) b;
                    var output = db.fcScanOutput();
                    var target = db.fcDrillTarget();
                    return new fakeBar(new detailBar(Float.NaN, 
                                                     () -> b.status() == BlockStatus.active ? db.fcCalcDrillSpeed(target, output.get(target, 0)) / 60f : 0f,
                                                     outlineIcons.findDrawable(target),
                                                     bundle.get("bar.drillspeed"),
                                                     target.color));
                };
            }
        } else if (name.equals("capacity")) {
            if (block instanceof CoreBlock) {
                return b -> new fakeBar(new detailBar(((float)((CoreBuild) b).storageCapacity * Vars.content.items().count(UnlockableContent::unlockedNow)),
                                                      () -> b.items.total(),
                                                      outlineIcons.totalItem,
                                                      bundle.get("bar.capacity"),
                                                      Pal.items));
            }
        }

        return null;
    }
}
