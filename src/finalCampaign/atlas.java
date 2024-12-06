package finalCampaign;

import java.lang.reflect.*;
import arc.*;
import arc.struct.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.graphics.g2d.TextureAtlas.*;
import arc.scene.style.*;
import arc.util.*;
import finalCampaign.feature.featureClass.spritePacker.*;
import finalCampaign.util.*;
import mindustry.*;
import mindustry.ctype.*;

public class atlas {
    private static ObjectMap<String, Drawable> drawables;
    private static ObjectMap<String, AtlasRegion> regionmap;
    private static Seq<AtlasRegion> regions;
    private static Fi spritePackerDataDir;
    private static  spritePacker.packCache cacheInfo;

    @SuppressWarnings("unchecked")
    public static void init() {
        try {
            Field field = Core.atlas.getClass().getDeclaredField("drawables");
            field.setAccessible(true);
            drawables = (ObjectMap<String, Drawable>) field.get(Core.atlas);
            regionmap = Core.atlas.getRegionMap();
            regions = Core.atlas.getRegions();
        } catch(Exception e) {
            Log.err(e);
            throw new RuntimeException(e);
        }

        spritePackerDataDir = finalCampaign.dataDir.child("spritePacker");
    }

    private static void load(Fi pack) {
        Fi packageFi = pack.child("package");
        if (!packageFi.exists()) return;

        spritePacker.packageInfo info = objectData.read(packageFi.readBytes(), spritePacker.packageInfo.class);
        Seq<Texture> texture = new Seq<>();
        Seq<Pixmap> pixmap = new Seq<>();

        boolean linear = Core.settings.getBool("linear", true);

        for (int i=0; i<info.textureNum; i++) pixmap.add(new Pixmap(pack.child("sprite-" + Integer.toString(i) + ".png")));
        for (int i=0; i<info.textureNum; i++) {
            Texture t = new Texture(pixmap.get(i));
            t.setFilter(linear ? TextureFilter.linear : TextureFilter.nearest);
            texture.add(t);
            Core.atlas.getTextures().add(t);
        };

        for (spritePacker.regionInfo rInfo : info.region) {
            Texture rTexture = texture.get(rInfo.texturePos);
            TextureAtlas.AtlasRegion region = new TextureAtlas.AtlasRegion(rTexture, rInfo.x, rInfo.y, rInfo.width, rInfo.height);

            if(rInfo.splits != null){
                region.splits = rInfo.splits;
                region.pads = rInfo.pads;
            }

            region.name = rInfo.name;
            region.offsetX = rInfo.offsetX;
            region.offsetY = rInfo.offsetY;
            region.originalWidth = rInfo.originalWidth;
            region.originalHeight = rInfo.originalHeight;

            if (drawables.containsKey(rInfo.name)) drawables.remove(rInfo.name);
            if (regionmap.containsKey(rInfo.name)) regionmap.remove(rInfo.name);
            
            AtlasRegion shouldBeRemovedRegion = null;
            for (AtlasRegion r : regions) {
                if (r.name.equals(rInfo.name)) {
                    shouldBeRemovedRegion = r;
                    break;
                }
            }
            if (shouldBeRemovedRegion != null) regions.remove(shouldBeRemovedRegion);

            Core.atlas.getRegions().add(region);
            Core.atlas.getRegionMap().put(rInfo.name, region);
        }
    }

    public static void load() {
        if (version.type.equals("debug")) {
            Fi cache = spritePackerDataDir.child("cache");
            if (cache.exists()) cacheInfo = objectData.read(cache.readBytes(), spritePacker.packCache.class);
        }

        Fi spriteDir = version.type.equals("debug") && cacheInfo != null ? new Fi(cacheInfo.outputDir) : finalCampaign.thisModFi.child("fcSprite");
        for (Fi dir : spriteDir.list()) load(dir);

        for (Seq<Content> arr : Vars.content.getContentMap()) {
            arr.each(c -> {
                if (c instanceof UnlockableContent u) {
                    u.load();
                    u.loadIcon();
                }
            });
        }

        if (cacheInfo == null) return;
        if (!cacheInfo.done && version.type.equals("debug")) {
            spritePacker.packGenerated(cacheInfo);
            Vars.ui.showOkText("Info", "Sprite pack done. See your output directory.", () -> {});
        }
    }

    public static AtlasRegion find(String name) {
        return Core.atlas.find("final-campaign-" + name);
    }
}
