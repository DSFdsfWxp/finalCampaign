package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import arc.*;
import arc.graphics.*;
import arc.input.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import finalCampaign.feature.featureClass.blockShortcut.*;
import finalCampaign.feature.featureClass.control.setMode.*;
import mindustry.*;
import mindustry.ai.*;
import mindustry.core.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.ui.fragments.*;
import mindustry.world.*;

import static mindustry.Vars.*;

@Mixin(PlacementFragment.class)
public abstract class fcPlacementFragment {
    @Shadow(remap = false)
    int rowWidth;
    @Shadow(remap = false)
    public Category currentCategory;
    @Shadow(remap = false)
    Seq<Block> returnArray, returnArray2;
    @Shadow(remap = false)
    Seq<Category> returnCatArray;
    @Shadow(remap = false)
    boolean[] categoryEmpty;
    @Shadow(remap = false)
    ObjectMap<Category,Block> selectedBlocks;
    @Shadow(remap = false)
    ObjectFloatMap<Category> scrollPositions;
    @Shadow(remap = false)
    @Nullable Block menuHoverBlock;
    @Shadow(remap = false)
    @Nullable Displayable hover;
    @Shadow(remap = false)
    @Nullable Building lastFlowBuild, nextFlowBuild;
    @Shadow(remap = false)
    @Nullable Object lastDisplayState;
    @Shadow(remap = false)
    @Nullable Team lastTeam;
    @Shadow(remap = false)
    boolean wasHovered;
    @Shadow(remap = false)
    Table blockTable, toggler, topTable, blockCatTable, commandTable;
    @Shadow(remap = false)
    Stack mainStack;
    @Shadow(remap = false)
    ScrollPane blockPane;
    @Shadow(remap = false)
    Runnable rebuildCommand;
    @Shadow(remap = false)
    boolean blockSelectEnd, wasCommandMode;
    @Shadow(remap = false)
    int blockSelectSeq;
    @Shadow(remap = false)
    long blockSelectSeqMillis;
    @Shadow(remap = false)
    Binding[] blockSelect;

    @Shadow(remap = false)
    abstract Seq<Block> getUnlockedByCategory(Category cat);
    @Shadow(remap = false)
    abstract boolean unlocked(Block block);
    @Shadow(remap = false)
    abstract Seq<Block> getByCategory(Category cat);
    @Shadow(remap = false)
    abstract Seq<Category> getCategories();
    @Shadow(remap = false)
    abstract Block getSelectedBlock(Category cat);
    @Shadow(remap = false)
    abstract boolean hasInfoBox();
    @Shadow(remap = false)
    abstract boolean gridUpdate(InputHandler input);

    private Runnable rebuildCategory;

    public void rebuildCategory() {
        if (rebuildCategory != null) rebuildCategory.run();
    }
    
    public void build(Group parent){
        parent.fill(full -> {
            toggler = full;
            full.bottom().right().visible(() -> ui.hudfrag.shown && !fSetMode.isOn());
            full.name = "fcPlacementFragment";

            full.table(frame -> {

                //rebuilds the category table with the correct recipes
                rebuildCategory = () -> {
                    blockTable.clear();
                    blockTable.top().margin(5);

                    int index = 0;

                    ButtonGroup<ImageButton> group = new ButtonGroup<>();
                    group.setMinCheckCount(0);

                    for(Block block : getUnlockedByCategory(currentCategory)){
                        if(!unlocked(block)) continue;
                        if(index++ % rowWidth == 0){
                            blockTable.row();
                        }

                        ImageButton button = blockTable.button(new TextureRegionDrawable(block.uiIcon), Styles.selecti, () -> {
                            if(unlocked(block)){
                                if((Core.input.keyDown(KeyCode.shiftLeft) || Core.input.keyDown(KeyCode.controlLeft)) && Fonts.getUnicode(block.name) != 0){
                                    Core.app.setClipboardText((char)Fonts.getUnicode(block.name) + "");
                                    ui.showInfoFade("@copied");
                                }else{
                                    control.input.block = control.input.block == block ? null : block;
                                    selectedBlocks.put(currentCategory, control.input.block);
                                }
                            }
                        }).size(46f).group(group).name("block-" + block.name).get();
                        button.resizeImage(iconMed);

                        button.update(() -> { //color unplacable things gray
                            Building core = player.core();
                            Color color = (state.rules.infiniteResources || (core != null && (core.items.has(block.requirements, state.rules.buildCostMultiplier) || state.rules.infiniteResources))) && player.isBuilder() ? Color.white : Color.gray;
                            button.forEach(elem -> elem.setColor(color));
                            button.setChecked(control.input.block == block);

                            if(!block.isPlaceable()){
                                button.forEach(elem -> elem.setColor(Color.darkGray));
                            }
                        });

                        button.hovered(() -> menuHoverBlock = block);
                        button.exited(() -> {
                            if(menuHoverBlock == block){
                                menuHoverBlock = null;
                            }
                        });
                    }
                    //add missing elements to even out table size
                    if(index < 4){
                        for(int i = 0; i < 4-index; i++){
                            blockTable.add().size(46f);
                        }
                    }
                    blockTable.act(0f);
                    blockPane.setScrollYForce(scrollPositions.get(currentCategory, 0));
                    Core.app.post(() -> {
                        blockPane.setScrollYForce(scrollPositions.get(currentCategory, 0));
                        blockPane.act(0f);
                        blockPane.layout();
                    });
                };

                //top table with hover info
                frame.table(Tex.buttonEdge2,top -> {
                    topTable = top;
                    top.add(new Table()).growX().update(topTable -> {

                        //find current hovered thing
                        Displayable hovered = hover;
                        Block displayBlock = menuHoverBlock != null ? menuHoverBlock : control.input.block;
                        Object displayState = displayBlock != null ? displayBlock : hovered;
                        boolean isHovered = displayBlock == null; //use hovered thing if displayblock is null

                        //don't refresh unnecessarily
                        //refresh only when the hover state changes, or the displayed block changes
                        if(wasHovered == isHovered && lastDisplayState == displayState && lastTeam == player.team()) return;

                        topTable.clear();
                        topTable.top().left().margin(5);

                        lastDisplayState = displayState;
                        wasHovered = isHovered;
                        lastTeam = player.team();

                        //show details of selected block, with costs
                        if(displayBlock != null){

                            topTable.table(header -> {
                                String keyCombo = "";
                                if(!mobile && !fBlockShortcut.disabledGameBlockSelect()){
                                    Seq<Block> blocks = getByCategory(currentCategory);
                                    for(int i = 0; i < blocks.size; i++){
                                        if(blocks.get(i) == displayBlock && (i + 1) / 10 - 1 < blockSelect.length){
                                            keyCombo = Core.bundle.format("placement.blockselectkeys", Core.keybinds.get(blockSelect[currentCategory.ordinal()]).key.toString())
                                                + (i < 10 ? "" : Core.keybinds.get(blockSelect[(i + 1) / 10 - 1]).key.toString() + ",")
                                                + Core.keybinds.get(blockSelect[i % 10]).key.toString() + "]";
                                            break;
                                        }
                                    }
                                }
                                final String keyComboFinal = keyCombo;
                                header.left();
                                header.add(new Image(displayBlock.uiIcon)).size(8 * 4);
                                header.labelWrap(() -> !unlocked(displayBlock) ? Core.bundle.get("block.unknown") : displayBlock.localizedName + keyComboFinal)
                                .left().width(190f).padLeft(5);
                                header.add().growX();
                                if(unlocked(displayBlock)){
                                    header.button("?", Styles.flatBordert, () -> {
                                        ui.content.show(displayBlock);
                                        Events.fire(new BlockInfoEvent());
                                    }).size(8 * 5).padTop(-5).padRight(-5).right().grow().name("blockinfo");
                                }
                            }).growX().left();
                            topTable.row();
                            //add requirement table
                            topTable.table(req -> {
                                req.top().left();

                                for(ItemStack stack : displayBlock.requirements){
                                    req.table(line -> {
                                        line.left();
                                        line.image(stack.item.uiIcon).size(8 * 2);
                                        line.add(stack.item.localizedName).maxWidth(140f).fillX().color(Color.lightGray).padLeft(2).left().get().setEllipsis(true);
                                        line.labelWrap(() -> {
                                            Building core = player.core();
                                            int stackamount = Math.round(stack.amount * state.rules.buildCostMultiplier);
                                            if(core == null || state.rules.infiniteResources) return "*/" + stackamount;

                                            int amount = core.items.get(stack.item);
                                            String color = (amount < stackamount / 2f ? "[scarlet]" : amount < stackamount ? "[accent]" : "[white]");

                                            return color + UI.formatAmount(amount) + "[white]/" + stackamount;
                                        }).padLeft(5);
                                    }).left();
                                    req.row();
                                }
                            }).growX().left().margin(3);

                            if(!displayBlock.isPlaceable() || !player.isBuilder()){
                                topTable.row();
                                topTable.table(b -> {
                                    b.image(Icon.cancel).padRight(2).color(Color.scarlet);
                                    b.add(!player.isBuilder() ? "@unit.nobuild" : !displayBlock.supportsEnv(state.rules.env) ? "@unsupported.environment" : "@banned").width(190f).wrap();
                                    b.left();
                                }).padTop(2).left();
                            }

                        }else if(hovered != null){
                            //show hovered item, whatever that may be
                            hovered.display(topTable);
                        }
                    });
                }).colspan(3).fillX().visible(this::hasInfoBox).touchable(Touchable.enabled).row();

                frame.image().color(Pal.gray).colspan(3).height(4).growX().row();

                blockCatTable = new Table();
                commandTable = new Table(Tex.pane2);
                mainStack = new Stack();

                mainStack.update(() -> {
                    if(control.input.commandMode != wasCommandMode){
                        mainStack.clearChildren();
                        mainStack.addChild(control.input.commandMode ? commandTable : blockCatTable);

                        //hacky, but forces command table to be same width as blocks
                        if(control.input.commandMode){
                            commandTable.getCells().peek().width(blockCatTable.getWidth() / Scl.scl(1f));
                        }

                        wasCommandMode = control.input.commandMode;
                    }
                });

                frame.add(mainStack).colspan(3).fill();

                frame.row();

                //for better inset visuals at the bottom
                frame.rect((x, y, w, h) -> {
                    if(Core.scene.marginBottom > 0){
                        Tex.paneLeft.draw(x, 0, w, y);
                    }
                }).colspan(3).fillX().row();

                //commandTable: commanded units
                {
                    commandTable.touchable = Touchable.enabled;
                    commandTable.add(Core.bundle.get("commandmode.name")).fill().center().labelAlign(Align.center).row();
                    commandTable.image().color(Pal.accent).growX().pad(20f).padTop(0f).padBottom(4f).row();
                    commandTable.table(u -> {
                        u.left();
                        int[] curCount = {0};
                        UnitCommand[] currentCommand = {null};
                        var commands = new Seq<UnitCommand>();

                        rebuildCommand = () -> {
                            u.clearChildren();
                            var units = control.input.selectedUnits;
                            if(units.size > 0){
                                int[] counts = new int[content.units().size];
                                for(var unit : units){
                                    counts[unit.type.id] ++;
                                }
                                commands.clear();
                                boolean firstCommand = false;
                                Table unitlist = u.table().growX().left().get();
                                unitlist.left();

                                int col = 0;
                                for(int i = 0; i < counts.length; i++){
                                    if(counts[i] > 0){
                                        var type = content.unit(i);
                                        unitlist.add(new ItemImage(type.uiIcon, counts[i])).tooltip(type.localizedName).pad(4).with(b -> {
                                            var listener = new ClickListener();

                                            //left click -> select
                                            b.clicked(KeyCode.mouseLeft, () -> {
                                                control.input.selectedUnits.removeAll(unit -> unit.type != type);
                                                Events.fire(Trigger.unitCommandChange);
                                            });
                                            //right click -> remove
                                            b.clicked(KeyCode.mouseRight, () -> {
                                                control.input.selectedUnits.removeAll(unit -> unit.type == type);
                                                Events.fire(Trigger.unitCommandChange);
                                            });

                                            b.addListener(listener);
                                            b.addListener(new HandCursorListener());
                                            //gray on hover
                                            b.update(() -> ((Group)b.getChildren().first()).getChildren().first().setColor(listener.isOver() ? Color.lightGray : Color.white));
                                        });

                                        if(++col % 7 == 0){
                                            unitlist.row();
                                        }

                                        if(!firstCommand){
                                            commands.add(type.commands);
                                            firstCommand = true;
                                        }else{
                                            //remove commands that this next unit type doesn't have
                                            commands.removeAll(com -> !Structs.contains(type.commands, com));
                                        }
                                    }
                                }

                                if(commands.size > 1){
                                    u.row();

                                    u.table(coms -> {
                                        for(var command : commands){
                                            coms.button(Icon.icons.get(command.icon, Icon.cancel), Styles.clearNoneTogglei, () -> {
                                                IntSeq ids = new IntSeq();
                                                for(var unit : units){
                                                    ids.add(unit.id);
                                                }

                                                Call.setUnitCommand(Vars.player, ids.toArray(), command);
                                            }).checked(i -> currentCommand[0] == command).size(50f).tooltip(command.localized());
                                        }
                                    }).fillX().padTop(4f).left();
                                }
                            }else{
                                u.add(Core.bundle.get("commandmode.nounits")).color(Color.lightGray).growX().center().labelAlign(Align.center).pad(6);
                            }
                        };

                        u.update(() -> {
                            boolean hadCommand = false;
                            UnitCommand shareCommand = null;

                            //find the command that all units have, or null if they do not share one
                            for(var unit : control.input.selectedUnits){
                                if(unit.isCommandable()){
                                    var nextCommand = unit.command().command;

                                    if(hadCommand){
                                        if(shareCommand != nextCommand){
                                            shareCommand = null;
                                        }
                                    }else{
                                        shareCommand = nextCommand;
                                        hadCommand = true;
                                    }
                                }
                            }

                            currentCommand[0] = shareCommand;

                            int size = control.input.selectedUnits.size;
                            if(curCount[0] != size){
                                curCount[0] = size;
                                rebuildCommand.run();
                            }
                        });
                        rebuildCommand.run();
                    }).grow();
                }

                //blockCatTable: all blocks | all categories
                {
                    blockCatTable.table(Tex.pane2, blocksSelect -> {
                        blocksSelect.margin(4).marginTop(0);
                        blockPane = blocksSelect.pane(blocks -> blockTable = blocks).height(194f).update(pane -> {
                            if(pane.hasScroll()){
                                Element result = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                                if(result == null || !result.isDescendantOf(pane)){
                                    Core.scene.setScrollFocus(null);
                                }
                            }
                        }).grow().get();
                        blockPane.setStyle(Styles.smallPane);
                        blocksSelect.row();
                        blocksSelect.table(control.input::buildPlacementUI).name("inputTable").growX();
                    }).fillY().bottom().touchable(Touchable.enabled);
                    blockCatTable.table(categories -> {
                        categories.bottom();
                        categories.add(new Image(Styles.black6){
                            @Override
                            public void draw(){
                                if(height <= Scl.scl(3f)) return;
                                getDrawable().draw(x, y, width, height - Scl.scl(3f));
                            }
                        }).colspan(2).growX().growY().padTop(-3f).row();
                        categories.defaults().size(50f);

                        ButtonGroup<ImageButton> group = new ButtonGroup<>();

                        //update category empty values
                        for(Category cat : Category.all){
                            Seq<Block> blocks = getUnlockedByCategory(cat);
                            categoryEmpty[cat.ordinal()] = blocks.isEmpty();
                        }

                        boolean needsAssign = categoryEmpty[currentCategory.ordinal()];

                        int f = 0;
                        for(Category cat : getCategories()){
                            if(f++ % 2 == 0) categories.row();

                            if(categoryEmpty[cat.ordinal()]){
                                categories.image(Styles.black6);
                                continue;
                            }

                            if(needsAssign){
                                currentCategory = cat;
                                needsAssign = false;
                            }

                            categories.button(ui.getIcon(cat.name()), Styles.clearTogglei, () -> {
                                currentCategory = cat;
                                if(control.input.block != null){
                                    control.input.block = getSelectedBlock(currentCategory);
                                }
                                rebuildCategory.run();
                            }).group(group).update(i -> i.setChecked(currentCategory == cat)).name("category-" + cat.name());
                        }
                    }).fillY().bottom().touchable(Touchable.enabled);
                }

                mainStack.add(blockCatTable);

                rebuildCategory.run();
                frame.update(() -> {
                    if(gridUpdate(control.input)) rebuildCategory.run();
                });
            });
        });
    }
}
