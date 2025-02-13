package finalCampaign.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.scene.style.*;
import finalCampaign.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.graphics.*;
import mindustry.graphics.MultiPacker.*;

public class fcOutlineIcon {
    public static TextureRegionDrawable health;
    public static TextureRegionDrawable ammoReload;
    public static TextureRegionDrawable battery;
    public static TextureRegionDrawable clock;
    public static TextureRegionDrawable crafting;
    public static TextureRegionDrawable efficiency;
    public static TextureRegionDrawable hammer;
    public static TextureRegionDrawable heat;
    public static TextureRegionDrawable lighting;
    public static TextureRegionDrawable loadCapacity;
    public static TextureRegionDrawable power;
    public static TextureRegionDrawable shields;
    public static TextureRegionDrawable totalItem;
    public static TextureRegionDrawable defaultCamera;
    public static TextureRegionDrawable followCamera;
    public static TextureRegionDrawable freeCamera;
    public static TextureRegionDrawable target;

    public static void load() {
        health = new TextureRegionDrawable(atlas.find("icon-health-outline"));
        ammoReload = new TextureRegionDrawable(atlas.find("icon-ammoReload-outline"));
        battery = new TextureRegionDrawable(atlas.find("icon-battery-outline"));
        clock = new TextureRegionDrawable(atlas.find("icon-clock-outline"));
        crafting = new TextureRegionDrawable(atlas.find("icon-crafting-outline"));
        efficiency = new TextureRegionDrawable(atlas.find("icon-efficiency-outline"));
        hammer = new TextureRegionDrawable(atlas.find("icon-hammer-outline"));
        heat = new TextureRegionDrawable(atlas.find("icon-heat-outline"));
        lighting = new TextureRegionDrawable(atlas.find("icon-lighting-outline"));
        loadCapacity = new TextureRegionDrawable(atlas.find("icon-loadCapacity-outline"));
        power = new TextureRegionDrawable(atlas.find("icon-power-outline"));
        shields = new TextureRegionDrawable(atlas.find("icon-shields-outline"));
        totalItem = new TextureRegionDrawable(atlas.find("icon-totalItem-outline"));
        defaultCamera = new TextureRegionDrawable(atlas.find("icon-defaultCamera-outline"));
        followCamera = new TextureRegionDrawable(atlas.find("icon-followCamera-outline"));
        freeCamera = new TextureRegionDrawable(atlas.find("icon-freeCamera-outline"));
        target = new TextureRegionDrawable(atlas.find("icon-target-outline"));
    }

    public static void generate() {
        final ContentType[] lst = {ContentType.block, ContentType.item, ContentType.unit, ContentType.liquid};
        MultiPacker packer = new MultiPacker();
        
        for (ContentType t : lst)
            for (Content c : Vars.content.getBy(t))
                if (c instanceof UnlockableContent uc) makeOutline(uc, packer);
        
        packer.flush(TextureFilter.linear, Core.atlas);
        packer.dispose();
    }

    public static TextureRegion findRegion(UnlockableContent content) {
        return Core.atlas.find(content.getContentType().toString() + "-" + content.name + "-outline");
    }

    public static Drawable findDrawable(UnlockableContent content) {
        return Core.atlas.drawable(content.getContentType().toString() + "-" + content.name + "-outline");
    }

    private static void makeOutline(UnlockableContent content, MultiPacker packer) {
        String name = content.getContentType().toString() + "-" + content.name + "-outline";
        if (Core.atlas.find(name).found()) return;

        PixmapRegion src = Core.atlas.getPixmap(content.uiIcon);
        Pixmap tmp = new Pixmap(src.width + 8, src.height + 8);
        tmp.draw(src, 4, 4);

        Pixmap pixmap = Pixmaps.outline(new PixmapRegion(tmp), Color.valueOf("3f3f3f"), 4);
        packer.add(PageType.main, name, pixmap);
        
        tmp.dispose();
        pixmap.dispose();
    }
}
