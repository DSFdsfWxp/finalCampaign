package finalCampaign.feature.featureClass.spritePacker;

import arc.*;
import arc.files.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import finalCampaign.*;
import finalCampaign.util.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.graphics.g2d.PixmapPacker.*;
import arc.graphics.g2d.TextureAtlas.*;
import mindustry.*;

public class spritePacker {
    
    private static PixmapPacker newPacker(int width, int height) {
        return new PixmapPacker(Math.min(Vars.maxTextureSize, width), Math.min(Vars.maxTextureSize, height), 2, true);
    }

    public static void pack(Fi rawAssetDir, Fi outputDir) {
        if (!rawAssetDir.exists() || !rawAssetDir.isDirectory())
            throw new RuntimeException("Not a valid raw asset directory: " + rawAssetDir.absolutePath());
        if (!outputDir.exists() || !outputDir.isDirectory()) outputDir.mkdirs();

        Seq<Fi> subDir = new Seq<>();
        Seq<String> regionNameLst = new Seq<>(Core.atlas.getRegionMap().keys().toSeq().copy());

        Cons<Fi> runPack = dir -> {
            String path = dir.absolutePath().substring(rawAssetDir.absolutePath().length());
            Fi pageFi = dir.child("page.json");
            TextureFilter filter = Core.settings.getBool("linear", true) ? TextureFilter.linear : TextureFilter.nearest;

            Log.info("spritePacker: enter: " + path);

            for (Fi subFi : dir.list()) if (subFi.isDirectory()) subDir.add(subFi);

            if (pageFi.exists()) {
                JsonReader reader = new JsonReader();
                JsonValue value = reader.parse(pageFi);
                PixmapPacker packer = newPacker(value.getInt("width"), value.getInt("height"));
                boolean prefix = value.getBoolean("prefix");
                Seq<Pixmap> rawPixmap = new Seq<>();

                dir.walk(subFi -> {
                    if (subFi.extension().toLowerCase().equals("png")) {
                        String name = (prefix ? "final-campaign-" : "") + subFi.nameWithoutExtension();
                        Log.info("spritePacker:   " + name);
                        Pixmap pixmap = new Pixmap(subFi);
                        rawPixmap.add(pixmap);
                        packer.pack(name, pixmap);
                    }
                });

                Fi output = outputDir.child(path.substring(1).replaceAll("/","-"));
                if (output.exists()) output.deleteDirectory();
                output.mkdirs();

                TextureAtlas atlas = packer.generateTextureAtlas(filter, filter, false);
                Seq<Pixmap> pixmap = new Seq<>();
                Seq<Texture> texture = new Seq<>();
                Seq<regionInfo> region = new Seq<>();

                for (Page page : packer.getPages()) {
                    pixmap.add(page.getPixmap());
                    texture.add(page.getTexture());
                }

                for (AtlasRegion r : atlas.getRegions()) {
                    int texturePos = texture.indexOf(r.texture, true);
                    if (texturePos < 0) throw new RuntimeException("texture not found.");

                    regionNameLst.add(r.name);

                    regionInfo info = new regionInfo();
                    info.texturePos = texturePos;
                    info.name = r.name;
                    info.u = r.u;
                    info.v = r.v;
                    info.u2 = r.u2;
                    info.v2 = r.v2;
                    info.width = r.width;
                    info.height = r.height;
                    info.scale = r.scale;

                    region.add(info);
                }

                for (Pixmap p : pixmap) {
                    PixmapIO.writePng(output.child("sprite-" + Integer.valueOf(pixmap.indexOf(p)).toString() + ".png"), p);
                    p.dispose();
                }

                for (Pixmap p : rawPixmap) p.dispose();

                packageInfo info = new packageInfo();
                info.textureNum = texture.size;
                info.region = region.toArray(regionInfo.class);

                output.child("package").writeBytes(objectData.write(info));
                Log.info("spritePacker:  -> " + output.absolutePath());
            }
        };
        
        runPack.get(rawAssetDir);

        while (subDir.size > 0) {
            Fi dir = subDir.first();
            runPack.get(dir);
            subDir.remove(0);
        }

        Fi dataDir = finalCampaign.dataDir.child("spritePacker");
        if (dataDir.exists()) dataDir.deleteDirectory();
        dataDir.mkdirs();

        packCache cache = new packCache();
        cache.rawAssetDir = rawAssetDir.absolutePath();
        cache.outputDir = outputDir.absolutePath();
        cache.regionNameLst = regionNameLst.toArray(String.class);
        cache.done = false;

        dataDir.child("cache").writeBytes(objectData.write(cache));
    }

    public static void packGenerated(packCache cache) {
        Fi outputDir = (new Fi(cache.outputDir)).child("generated");
        Seq<String> lst = new Seq<>(cache.regionNameLst);

        Seq<Pixmap> rawPixmaps = new Seq<>();
        ObjectMap<String, AtlasRegion> map = Core.atlas.getRegionMap();
        PixmapPacker packer = newPacker(2048, 2048);
        TextureFilter filter = Core.settings.getBool("linear", true) ? TextureFilter.linear : TextureFilter.nearest;

        Log.info("spritePacker: start to pack generated sprite");

        for (String name : map.keys()) {
            if (lst.contains(name)) continue;
            AtlasRegion r = map.get(name);
            Pixmap p = r.pixmapRegion.crop();

            Log.info("spritePacker:   " + name);
            packer.pack(name, p);
            rawPixmaps.add(p);
        }

        if (!outputDir.exists()) outputDir.mkdirs();

        TextureAtlas atlas = packer.generateTextureAtlas(filter, filter, false);
        Seq<regionInfo> regions = new Seq<>();
        Seq<Pixmap> pixmaps = new Seq<>();
        Seq<Texture> textures = new Seq<>();

        for (Page page : packer.getPages()) {
            pixmaps.add(page.getPixmap());
            textures.add(page.getTexture());
        }

        for (AtlasRegion r : atlas.getRegions()) {
            int texturePos = textures.indexOf(r.texture, true);
            if (texturePos < 0) throw new RuntimeException("texture not found.");

            regionInfo info = new regionInfo();
            info.texturePos = texturePos;
            info.name = r.name;
            info.u = r.u;
            info.v = r.v;
            info.u2 = r.u2;
            info.v2 = r.v2;
            info.width = r.width;
            info.height = r.height;
            info.scale = r.scale;

            regions.add(info);
        }

        for (Pixmap p : pixmaps) {
            PixmapIO.writePng(outputDir.child("sprite-" + Integer.valueOf(pixmaps.indexOf(p)).toString() + ".png"), p);
            p.dispose();
        }

        for (Pixmap p : rawPixmaps) p.dispose();

        packageInfo info = new packageInfo();
        info.textureNum = textures.size;
        info.region = regions.toArray(regionInfo.class);

        outputDir.child("package").writeBytes(objectData.write(info));
        Log.info("spritePacker:  -> " + outputDir.absolutePath());

        cache.done = true;

        Fi dataDir = finalCampaign.dataDir.child("spritePacker");
        if (!dataDir.exists()) dataDir.mkdirs();
        Fi cacheFi = dataDir.child("cache");
        if (cacheFi.exists()) cacheFi.delete();
        cacheFi.writeBytes(objectData.write(cache));
    }

    public static class packageInfo {
        public int textureNum;
        public regionInfo[] region;
    }

    public static class regionInfo {
        public String name;
        public int texturePos;
        public float u;
        public float v;
        public float u2;
        public float v2;
        public int width;
        public int height;
        public float scale;
    }

    public static class packCache {
        public String rawAssetDir;
        public String outputDir;
        public String[] regionNameLst;
        public boolean done;
    }
}
