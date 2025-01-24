package finalCampaign.feature.roulette;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import finalCampaign.*;
import finalCampaign.feature.blockShortcut.*;
import finalCampaign.ui.layout.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.world.*;

public class rouletteFragment extends fragment {
    private TextureRegion base;
    private TextureRegion focus;
    private boolean fourSlot;

    public float mouseX, mouseY;
    public Block selectedBlock;

    public rouletteFragment() {
        fourSlot = fRoulette.usingFourSlot();
        mouseX = 0f;
        mouseY = 0f;
        selectedBlock = null;
    }

    private boolean invalidBlock(Block block) {
        return !block.isPlaceable() || (!block.unlockedNow() && !((Vars.state == null || Vars.state.rules.infiniteResources) || Vars.state.rules.editor)) || !block.environmentBuildable() || !block.placeablePlayer;
    }

    @Override
    public void draw() {
        if (fourSlot != fRoulette.usingFourSlot()) {
            fourSlot = !fourSlot;
            base = focus = null;
        }
        if (base == null) base = atlas.find("roulette-" + (fourSlot ? "4" : "8"));
        if (focus == null) focus = atlas.find("roulette-" + (fourSlot ? "4" : "8") + "-focus");

        float size = Mathf.round(Math.min(Core.graphics.getHeight(), Core.graphics.getWidth()) * 0.1778f) * fRoulette.scale();
        Vec2 pos = Core.input.mouse().sub(mouseX, mouseY);

        int r = -1;
        int rStep;
        float mr = pos.angle();
        selectedBlock = null;
        if (fourSlot) {
            if ((mr < 45f && mr > 0f) || (mr < 360f && mr > 315f)) r = 0;
            if (mr < 135f && mr > 45f) r = 90;
            if (mr < 225f && mr > 135f) r = 180;
            if (mr < 315f && mr > 225f) r = 270;
            rStep = 90;
            if (r >= 0) selectedBlock = fBlockShortcut.getShortcutSlot(r / 90);
        } else {
            if ((mr < 22.5f && mr > 0f) || (mr < 360f && mr > 337.5f)) r = 0;
            if (mr < 67.5f && mr > 22.5f) r = 45;
            if (mr < 112.5f && mr > 67.5f) r = 90;
            if (mr < 157.5f && mr > 112.5f) r = 135;
            if (mr < 202.5f && mr > 157.5f) r = 180;
            if (mr < 247.5f && mr > 202.5f) r = 225;
            if (mr < 292.5f && mr > 247.5f) r = 270;
            if (mr < 337.5f && mr > 292.5f) r = 315;
            rStep = 45;
            if (r >= 0) selectedBlock = fBlockShortcut.getShortcutSlot(r / 45);
        }
        if (pos.len() < size * 0.1949f) selectedBlock = null;
        if (selectedBlock != null) if (invalidBlock(selectedBlock)) selectedBlock = null;

        Draw.color();
        Draw.alpha(this.color.a);
        if (r < 0 || selectedBlock == null) {
            Draw.rect(base, mouseX, mouseY, size, size);
        } else {
            Draw.rect(focus, mouseX, mouseY, size, size, r);
        }
        Draw.color();

        int len = fourSlot ? 4 : 8;
        float iSize = size / 272f * 36f;
        for (int i=0; i<len; i++) {
            Block block = fBlockShortcut.getShortcutSlot(i);
            if (block == null) continue;
            Vec2 iPos = new Vec2(size * 0.3456f, 0f);
            iPos.rotate(i * rStep);
            iPos.add(mouseX, mouseY);
            Building core = Vars.player.core();
            Color color = (Vars.state.rules.infiniteResources || (core != null && (core.items.has(block.requirements, Vars.state.rules.buildCostMultiplier) || Vars.state.rules.infiniteResources))) && Vars.player.isBuilder() ? Color.white : Color.gray;
            if (invalidBlock(block)) color = Color.darkGray;
            Draw.color(color);
            Draw.alpha(color.a * this.color.a);
            Draw.rect(block.fullIcon, iPos.x, iPos.y, iSize, iSize);
            Draw.color();
        }

    }

}
