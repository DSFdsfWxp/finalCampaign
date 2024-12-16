package finalCampaign.graphics;

import arc.scene.style.*;
import finalCampaign.atlas;

public class icons {
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

    public static void load() {
        health = new TextureRegionDrawable(atlas.find("icon-health"));
        ammoReload = new TextureRegionDrawable(atlas.find("icon-ammoReload"));
        battery = new TextureRegionDrawable(atlas.find("icon-battery"));
        clock = new TextureRegionDrawable(atlas.find("icon-clock"));
        crafting = new TextureRegionDrawable(atlas.find("icon-crafting"));
        efficiency = new TextureRegionDrawable(atlas.find("icon-efficiency"));
        hammer = new TextureRegionDrawable(atlas.find("icon-hammer"));
        heat = new TextureRegionDrawable(atlas.find("icon-heat"));
        lighting = new TextureRegionDrawable(atlas.find("icon-lighting"));
        loadCapacity = new TextureRegionDrawable(atlas.find("icon-loadCapacity"));
        power = new TextureRegionDrawable(atlas.find("icon-power"));
        shields = new TextureRegionDrawable(atlas.find("icon-shields"));
        totalItem = new TextureRegionDrawable(atlas.find("icon-totalItem"));
    }
}
