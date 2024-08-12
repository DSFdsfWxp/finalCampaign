package finalCampaign.feature.featureClass.control.setMode;

import arc.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import finalCampaign.*;
import finalCampaign.bundle.*;
import finalCampaign.feature.featureClass.wiki.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.game.EventType.*;
import mindustry.ui.*;

public class setModeFragment extends Table {

    private ButtonGroup<ImageButton> group;
    protected boolean forceSelectOpt;
    protected Seq<iFeature> features;
    protected Seq<String> categories;

    public setModeFragment() {
        this.name = "fcSetModeFragment";
        setBackground(Tex.pane);
        visible(() -> Vars.ui.hudfrag.shown && !Vars.control.input.commandMode && fSetMode.isOn());
        setWidth(Scl.scl(295f));
        right().bottom();
        group = new ButtonGroup<>();
        group.setMaxCheckCount(Category.all.length);
        group.setMinCheckCount(0);
        forceSelectOpt = false;
        features = new Seq<>();
        categories = new Seq<>();
    }

    public void rebuild() {
        clear();

        if (fSetMode.selecting || (!fSetMode.selecting && fSetMode.selected.size == 0) || forceSelectOpt) {
            table(t -> {
                t.add(bundle.get("setMode.title")).center().fillX().color(Pal.accent).labelAlign(Align.center).row();
                t.image().color(Pal.accent).growX().pad(20f).padTop(0f).padBottom(8f).row();

                t.table(opt -> {
                    opt.add(bundle.get("setMode.selecting.filter")).left().padBottom(4f).colspan(3).row();
                    opt.table(filter -> {
                        int count = 0;
                        for (Category cat : Category.all) {
                            filter.button(Vars.ui.getIcon(cat.name()), Styles.clearTogglei, () -> {
                                Core.app.post(() -> {
                                    fSetMode.selectFilter.clear();
                                    for (ImageButton b : group.getAllChecked()) fSetMode.selectFilter.add(Category.valueOf(b.name));
                                });
                            }).name(cat.name()).center().group(group).size(46f).update(f -> f.setChecked(fSetMode.selectFilter.contains(cat)));
                            count ++;
                            if (count % 5 == 0) filter.row();
                        }
                    }).center().padBottom(8f).colspan(3).growX().row();

                    opt.add(bundle.get("setMode.selecting.selectSameBlockBuilding")).width(100f).left().padBottom(4f);
                    fWiki.setupWikiButton("setMode.selecting.selectSameBlockBuilding", opt.button("?", () -> {}).width(40f).right().padRight(4f).padBottom(4f).get());
                    {
                        TextButton button = new TextButton("null");
                        Runnable updateButton = () -> button.setText(bundle.get(fSetMode.selectSameBlockBuilding ? "on" : "off"));
                        button.clicked(() -> {
                            fSetMode.selectSameBlockBuilding = !fSetMode.selectSameBlockBuilding;
                            updateButton.run();
                        });
                        opt.add(button).width(50f).padBottom(4f).right();
                        updateButton.run();
                    }
                    opt.row();

                    opt.add(bundle.get("setMode.selecting.mode")).width(100f).left().padBottom(4f);
                    {
                        TextButton button = new TextButton("null");
                        Runnable updateButton = () -> button.setText(bundle.get(fSetMode.deselect ? "setMode.selecting.mode.deselect" : "setMode.selecting.mode.select"));
                        button.clicked(() -> {
                            fSetMode.deselect = !fSetMode.deselect;
                            updateButton.run();
                        });
                        opt.add(button).width(50f).padBottom(4f).colspan(2).right();
                        updateButton.run();
                    }
                }).width(295f);
            }).margin(4f).growX();
        } else {
            table(t -> {
                t.pane(cont -> {
                    cont.margin(5f);

                    boolean multiSelect = fSetMode.selected.size > 1;
                    Building firstBuilding = fSetMode.selected.get(0);
                    Building[] selected = fSetMode.selected.toArray(Building.class);
    
                    if (!multiSelect) cont.button("?", Styles.flatBordert, () -> {
                        Vars.ui.content.show(firstBuilding.block);
                        Events.fire(new BlockInfoEvent());
                    }).size(8 * 5).padBottom(4f).right().row();
    
                    cont.image(firstBuilding.block.fullIcon).center().scaling(Scaling.fit).size(128f).row();
                    if (multiSelect) cont.add("+" + Integer.toString(fSetMode.selected.size - 1)).center().padTop(-5f).padRight(-118f).color(Pal.accent).row();
    
                    if (!multiSelect) cont.add(firstBuilding.block.localizedName).center().color(Pal.accent).fontScale(1.2f).padTop(4f).row();
                    
                    for (String cat : categories) {
                        bundleNS bundle = new bundleNS("setMode.feature." + cat);
                        boolean inited = false;

                        for (iFeature feature : features) {
                            if (!cat.equals(feature.category)) continue;
                            if (selected.length > 1 && !feature.supportMultiSelect) continue;
                            if (feature.isSupported(selected)) {
                                if (!inited) {
                                    cont.image().color(Pal.accent).growX().pad(20f).padTop(8f).padBottom(4f).row();
                                    cont.add(bundle.get("name")).center().color(Pal.accent).padBottom(8f).row();
                                    inited = true;
                                }
                                cont.table(table -> feature.buildUI(selected, table, bundle.appendNS(feature.name))).padBottom(8f).left().growX().row();
                            }
                        }
                    }
                }).scrollX(false).grow();
            }).fill().height(Core.graphics.getHeight() / Scl.scl() - 10f).width(285f);
        }
    }
}
