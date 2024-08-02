package finalCampaign.feature.featureClass.control.shortcut;

import java.lang.reflect.*;
import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import finalCampaign.feature.featureClass.blockShortcut.*;
import finalCampaign.feature.featureClass.blockShortcut.fBlockShortcut.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.*;

public class shortcutTable extends Table {
    private ButtonGroup<ImageButton> group;
    private Block[] blocks;
    private TextureRegion blank;
    private Field hoverBlock;
    
    public shortcutTable() {
        this.name = "fcShortcut";
        super.visibility = () -> Vars.ui.hudfrag.shown && fShortcut.isOn() && !Vars.control.input.commandMode;
        setBackground(Tex.pane);
        setSize(Scl.scl(248f), Scl.scl(110f));
        blank = Core.atlas.find("clear-effect");
        super.update(() -> {
            super.x = fShortcut.getX() - width;
            super.y = fShortcut.getY();
        });

        try {
            hoverBlock = Vars.ui.hudfrag.blockfrag.getClass().getDeclaredField("menuHoverBlock");
            hoverBlock.setAccessible(true);
        } catch(Exception e) {
            Log.err(e);
        }

        group = new ButtonGroup<>();
        group.setMinCheckCount(0);
        blocks = new Block[10];
        table(blockTable -> {
            for (int i=0; i<10; i++) {
                int pos = i;
                Block block = fBlockShortcut.getShortcutSlot(i);
                blocks[i] = block;

                ImageButton button = blockTable.button(new TextureRegionDrawable(block == null ? blank : block.uiIcon), Styles.selecti, () -> {
                    if (blocks[pos] != null) {
                        if((Core.input.keyDown(KeyCode.shiftLeft) || Core.input.keyDown(KeyCode.controlLeft)) && Fonts.getUnicode(blocks[pos].name) != 0){
                            Core.app.setClipboardText((char)Fonts.getUnicode(blocks[pos].name) + "");
                            Vars.ui.showInfoFade("@copied");
                        }else{
                            if (!invalidBlock(blocks[pos])) Vars.control.input.block = Vars.control.input.block == blocks[pos] ? null : blocks[pos];
                            if (Vars.control.input.block != null) {
                                Vars.ui.hudfrag.blockfrag.currentCategory = Vars.control.input.block.category;
                                Reflect.invoke(Vars.ui.hudfrag.blockfrag, "rebuildCategory");
                            }
                        }
                    }
                    blockTable.act(0f);
                }).size(46f).group(group).name(blocks[pos] == null ? "null-" + pos : "block-" + blocks[pos].name).get();
                button.resizeImage(32f);

                button.clicked(KeyCode.mouseRight, () -> {
                    fBlockShortcut.clearBlockSlot(pos);
                });

                button.update(() -> {
                    if (blocks[pos] == null) {
                        button.setChecked(false);
                        return;
                    }

                    Building core = Vars.player.core();
                    Color color = (Vars.state.rules.infiniteResources || (core != null && (core.items.has(blocks[pos].requirements, Vars.state.rules.buildCostMultiplier) || Vars.state.rules.infiniteResources))) && Vars.player.isBuilder() ? Color.white : Color.gray;
                    button.forEach(elem -> elem.setColor(color));
                    if (!group.getChecked().name.equals(button.name)) button.setChecked(Vars.control.input.block == blocks[pos]);

                    if(invalidBlock(blocks[pos])){
                        button.forEach(elem -> elem.setColor(Color.darkGray));
                    }
                });

                button.hovered(() -> {
                    if (hoverBlock != null && blocks[pos] != null) {
                        try {
                            hoverBlock.set(Vars.ui.hudfrag.blockfrag, blocks[pos]);
                            fBlockShortcut.forceIgnoreCheck = true;
                        } catch(Exception ignore) {}
                    }
                });

                button.exited(() -> {
                    if (hoverBlock != null && blocks[pos] != null) {
                        try {
                            if (hoverBlock.get(Vars.ui.hudfrag.blockfrag) == blocks[pos]) {
                                hoverBlock.set(Vars.ui.hudfrag.blockfrag, null);
                                fBlockShortcut.forceIgnoreCheck = false;
                            }
                        } catch(Exception ignore) {}
                    }
                });

                if (i == 4) blockTable.row();

                Events.on(shortcutChangeEvent.class, e -> {
                    if (e.id != pos) return;
                    blocks[pos] = e.block;
                    TextureRegion icon = e.block == null ? blank : e.block.uiIcon;
                    button.getStyle().imageUp = new TextureRegionDrawable(icon);
                    button.name = e.block == null ? "null-" + pos : "block-" + e.block.name;
                });
            }
        }).margin(9f);
    }

    private boolean invalidBlock(Block block) {
        return !block.isPlaceable() || (!block.unlocked() && !((Vars.state == null || Vars.state.rules.infiniteResources) || Vars.state.rules.editor)) || !block.environmentBuildable();
    }
}
