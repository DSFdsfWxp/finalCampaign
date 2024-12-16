package finalCampaign.feature.featureClass.control.setMode;

import arc.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import finalCampaign.*;
import finalCampaign.bundle.*;
import finalCampaign.feature.featureClass.wiki.*;
import finalCampaign.ui.*;
import finalCampaign.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.game.EventType.*;
import mindustry.ui.*;
import mindustry.world.*;

public class setModeFragment extends Table {

    private ButtonGroup<ImageButton> group;
    protected ScrollPane pane;
    protected boolean forceSelectOpt;
    protected Seq<IFeature> features;
    protected Seq<String> categories;

    public setModeFragment() {
        this.name = "fcSetModeFragment";
        touchable = Touchable.childrenOnly;
        visible(() -> Vars.ui.hudfrag.shown && !Vars.control.input.commandMode && fSetMode.isOn());
        right().bottom();
        group = new ButtonGroup<>();
        group.setMaxCheckCount(-1);
        group.setMinCheckCount(0);
        forceSelectOpt = false;
        features = new Seq<>();
        categories = new Seq<>();
    }

    public void rebuild() {
        clear();

        if (fSetMode.selecting || (!fSetMode.selecting && fSetMode.selected.size == 0) || forceSelectOpt) {
            table(t -> {
                t.setBackground(Tex.pane);
                t.add(bundle.get("setMode.title")).center().fillX().color(Pal.accent).labelAlign(Align.center).row();
                t.image().color(Pal.accent).growX().padTop(0f).padBottom(8f).row();

                t.table(opt -> {
                    opt.add(bundle.get("setMode.selecting.filter")).left().padBottom(4f).colspan(3).row();
                    opt.table(filter -> {
                        int count = 0;
                        group.uncheckAll();
                        for (Category cat : Category.all) {
                            filter.button(Vars.ui.getIcon(cat.name()), Styles.clearTogglei, () -> {
                                Core.app.post(() -> {
                                    fSetMode.selectFilter.clear();
                                    for (ImageButton b : group.getAllChecked()) fSetMode.selectFilter.add(Category.valueOf(b.name));
                                });
                            }).name(cat.name()).center().group(group).size(46f).get().setChecked(fSetMode.selectFilter.contains(cat));
                            count ++;
                            if (count % 5 == 0) filter.row();
                        }
                    }).center().padBottom(8f).colspan(3).growX().row();

                    opt.add(bundle.get("setMode.selecting.selectSameBlockBuilding")).grow().wrap().left().padBottom(4f).padRight(4f);
                    fWiki.setupWikiButton("setMode.selecting.selectSameBlockBuilding", opt.button("?", () -> {}).width(45f).expandX().right().padRight(4f).padBottom(4f).get());
                    {
                        TextButton button = new TextButton("null");
                        Runnable updateButton = () -> button.setText(bundle.get(fSetMode.selectSameBlockBuilding ? "on" : "off"));
                        button.clicked(() -> {
                            fSetMode.selectSameBlockBuilding = !fSetMode.selectSameBlockBuilding;
                            updateButton.run();
                        });
                        opt.add(button).width(75f).padBottom(4f).right();
                        updateButton.run();
                    }
                }).width(295f);
            }).margin(16f).growX();
        } else {
            table(t -> {
                t.setBackground(Tex.pane);

                pane = t.pane(cont -> {
                    setWidth(327f);
                    cont.margin(5f);

                    boolean multiSelect = fSetMode.selected.size > 1;
                    Building firstBuilding = fSetMode.selected.get(0);
                    fakeFinal<Building[]> selected = new fakeFinal<>(fSetMode.selected.toArray(Building.class));
    
                    if (!multiSelect) cont.button("?", Styles.flatBordert, () -> {
                        Vars.ui.content.show(firstBuilding.block);
                        Events.fire(new BlockInfoEvent());
                    }).size(8 * 5).padBottom(4f).right().row();
    
                    Image icon = cont.image(firstBuilding.block.fullIcon).center().scaling(Scaling.fit).size(128f).get();
                    cont.row();
                    Table it = cont.table().growX().get();
                    cont.row();
                    Table ft = cont.table().growX().get();

                    Runnable rebuiltFeatures = () -> {
                        ft.clear();
                        for (String cat : categories) {
                            bundleNS bundle = new bundleNS("setMode.feature." + cat);
                            boolean inited = false;
    
                            for (IFeature feature : features) {
                                if (!cat.equals(feature.category)) continue;
                                if (selected.get().length > 1 && !feature.supportMultiSelect) continue;
                                if (feature.isSupported(selected.get())) {
                                    if (!inited) {
                                        ft.image().color(Pal.accent).growX().padTop(8f).padBottom(4f).row();
                                        ft.add(bundle.get("name")).center().color(Pal.accent).padBottom(8f).row();
                                        inited = true;
                                    }
                                    ft.table(table -> feature.buildUI(selected.get(), table, bundle.appendNS(feature.name))).padBottom(8f).growX().row();
                                }
                            }
                        }
                    };

                    if (multiSelect) {
                        Label numLabel = it.add("+" + Integer.toString(fSetMode.selected.size - 1)).center().padTop(-5f).padRight(-118f).expandX().color(Pal.accent).padBottom(8f).get();
                        it.row();
                        Table ibt = it.table().growX().padBottom(4f).get();
                        ibt.left();

                        ButtonGroup<Button> group = new ButtonGroup<>();
                        ObjectIntMap<Block> map = new ObjectIntMap<>();
                        int c = 0;
                        group.setMinCheckCount(0);
                        group.setMaxCheckCount(0);
                        for (Building b : selected.get()) if (b.block != null) map.increment(b.block, 0, 1);

                        Runnable updateSelected = () -> {
                            Seq<Button> allChecked = group.getAllChecked();
                            Building[] allSelected = fSetMode.selected.toArray(Building.class);
                            Seq<Building> currentSelected = new Seq<>();
                            fSetMode.selectedBlock.clear();

                            if (allSelected.length == 0) {
                                rebuild();
                            } else {
                                for (Button b : allChecked) {
                                    Block block = Vars.content.block(b.name);
                                    if (block != null && !fSetMode.selectedBlock.contains(block)) fSetMode.selectedBlock.add(block);
                                }

                                if (allChecked.size > 0) {
                                    int bc = 0;
                                    for (Building b : allSelected) {
                                        if (b.block == null || !fSetMode.selectedBlock.contains(b.block)) continue;
                                        currentSelected.add(b);
                                        bc ++;
                                    }
                                    selected.set(currentSelected.toArray(Building.class));
                                    numLabel.setText("+" + Integer.toString(bc - 1));
                                    icon.setDrawable(currentSelected.size == 0 || currentSelected.get(0).block == null ? Core.atlas.find("error") : currentSelected.get(0).block.fullIcon);
                                } else {
                                    selected.set(allSelected);
                                    numLabel.setText("+" + Integer.toString(allSelected.length - 1));
                                    icon.setDrawable(allSelected[0].block == null ? Core.atlas.find("error") : allSelected[0].block.fullIcon);
                                }
                                rebuiltFeatures.run();
                            }
                        };

                        for (Block b : map.keys()) {
                            Button butt = new Button(Styles.selecti);
                            itemImage image = new itemImage(b.uiIcon, map.get(b));
                            butt.add(image).center().size(32f).scaling(Scaling.fit).grow();
                            butt.clicked(() -> Core.app.post(updateSelected));
                            ibt.add(butt).size(46f).name(b.name).group(group).left().tooltip(b.localizedName);
                            butt.setChecked(fSetMode.selectedBlock.contains(b));
                            if (++ c % 6 == 0) ibt.row();
                        }
                        updateSelected.run();
                    } else {
                        it.add(firstBuilding.block.localizedName).center().color(Pal.accent).fontScale(1.2f).padTop(4f).row();
                        rebuiltFeatures.run();
                    }

                }).scrollX(false).style(Styles.smallPane).grow().get();
                pane.setFadeScrollBars(true);
            }).growY().width(327f);
        }
    }
}
