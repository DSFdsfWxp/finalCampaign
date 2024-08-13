package finalCampaign.feature.featureClass.control.setMode.setFeature;

import java.io.*;
import arc.graphics.*;
import arc.input.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import finalCampaign.*;
import finalCampaign.feature.featureClass.buildTargeting.*;
import finalCampaign.feature.featureClass.buildTargeting.fcSortf.*;
import finalCampaign.bundle.*;
import finalCampaign.net.*;
import finalCampaign.patch.*;
import finalCampaign.ui.*;
import finalCampaign.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class targetingPriority extends bAttributeSetter {
    public targetingPriority() {
        super("targetingPriority", "set", false);
        supportMultiSelect = true;
        background = false;
    }

    public boolean init(Building[] selected) {
        for (Building building : selected) if (building instanceof TurretBuild) return true;
        return false;
    }

    public void buildUI(Building[] selected, Table table) {
        Building first = selected[0];
        IFcTurretBuild fcFirst = null;
        for (Building b : selected) if (b instanceof IFcTurretBuild itb) fcFirst = itb;
        IFcLiquidTurretBuild fcLiquidFirst = first instanceof IFcLiquidTurretBuild b ? b : null;

        presetsTable presetsTable = new presetsTable(selected);
        ButtonGroup<TextButton> presetsGroup = new ButtonGroup<>();
        Collapser presetsTableCol = new Collapser(presetsTable, true);
        priorityAddTable priorityAddTable = new priorityAddTable(fcFirst.fcSortf());
        Collapser priorityAddTableCol = new Collapser(priorityAddTable, true);
        priorityTable priorityTable = new priorityTable(fcFirst.fcSortf(), priorityAddTableCol);
        checkSettingTable preferBuildingTargetCheck = new checkSettingTable(bundleNS.get("preferBuildingTarget"));
        checkSettingTable preferExthinguishCheck = new checkSettingTable(bundleNS.get("preferExtinguish"));
        sideSelectTable sideSelectTable = new sideSelectTable(bundleNS);

        preferBuildingTargetCheck.setChecked(fcFirst.fcPreferBuildingTarget());
        preferBuildingTargetCheck.changed(() -> {
            fcCall.setTurretPreferBuildingTarget(first, preferBuildingTargetCheck.checked());
        });

        if (fcLiquidFirst != null) {
            preferExthinguishCheck.setChecked(fcLiquidFirst.fcPreferExtinguish());
            preferExthinguishCheck.changed(() -> {
                fcCall.setTurretPreferExtinguish(first, preferExthinguishCheck.checked());
            });
        }

        presetsTable.applied(() -> priorityTable.rebuild());
        priorityAddTable.added(() -> priorityTable.rebuild());
        priorityTable.deleted(() -> priorityAddTable.rebuild());
        sideSelectTable.selected(() -> {
            priorityTable.setSide(sideSelectTable.unit);
            priorityAddTable.setSide(sideSelectTable.unit);
        });

        presetsGroup.setMinCheckCount(0);
        table.button(bundleNS.get("preset"), () -> {
            presetsTableCol.toggle();
        }).group(presetsGroup).growX().with(t -> t.setStyle(Styles.togglet)).row();

        table.add(presetsTableCol).center().growX().row();

        table.add(preferBuildingTargetCheck).center().growX().visible(() -> selected.length == 1).row();
        table.add(preferExthinguishCheck).center().growX().visible(() -> selected.length == 1 && fcLiquidFirst != null).row();

        table.add(sideSelectTable).center().growX().visible(() -> selected.length == 1).row();

        table.add(priorityTable).center().growX().visible(() -> selected.length == 1).row();

        table.add(priorityAddTableCol).center().growX().visible(() -> selected.length == 1).row();
    }

    public static class sideSelectTable extends Table {
        Runnable selected;
        boolean unit;

        public sideSelectTable(bundleNS bundleNS) {
            selected = () -> {};
            unit = true;
            ButtonGroup<TextButton> group = new ButtonGroup<>();
            setBackground(Tex.sliderBack);

            table(inner -> {
                inner.button(bundleNS.get("unit"), Styles.flatTogglet, () -> {
                    if (unit) return;
                    unit = true;
                    selected.run();
                }).group(group).growX();
                inner.button(bundleNS.get("building"), Styles.flatTogglet, () -> {
                    if (!unit) return;
                    unit = false;
                    selected.run();
                }).group(group).growX();
            }).pad(4f).growX();
        }

        public void selected(Runnable run) {
            selected = run;
        }
    }

    public static class priorityTable extends Table {
        fcSortf sortf;
        Table inner;
        int lastCount;
        Runnable deleted;
        dragLayout layout;
        boolean unitSide;
        Seq<baseSortf<?>> current;
        ObjectMap<Table, baseSortf<?>> map;
        addItem add;

        public priorityTable(fcSortf sortf, Collapser col) {
            this.sortf = sortf;
            map = new ObjectMap<>();
            deleted = () -> {};
            unitSide = true;
            current = sortf.unitSortfs;
            add = new addItem(col);

            setBackground(Tex.sliderBack);
            table(t -> inner = t).pad(4f).growX();
            layout = new dragLayoutY(8f, 160f);
            inner.add(layout).growX();
            inner.add(add).growX();

            layout.indexUpdate(() -> {
                current.clear();
                for (Element e : layout.getChildren()) current.add(map.get((Table) e));
                updateSortf();
            });

            rebuild();
            inner.changed(this::changed);
        }

        public void setSide(boolean unit) {
            unitSide = unit;
            current = unit ? sortf.unitSortfs : sortf.buildSortfs;
            rebuild();
            add.reset();
            lastCount = current.size;
        }

        public void deleted(Runnable run) {
            deleted = run;
        }

        public void updateSortf() {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Writes writes = new Writes(new DataOutputStream(stream));
            sortf.write(writes);
            writes.close();
            fcCall.setBuildingSortf(sortf.build, stream.toByteArray());
        }

        public void changed() {
            if (current.size < lastCount) {
                deleted.run();
                rebuild();
                lastCount = current.size;
            }

            updateSortf();
        }

        public void rebuild() {
            layout.clear();
            map.clear();

            for (baseSortf<?> s : current) {
                priorityItem item = new priorityItem(layout, s, this);
                item.setWidth(layout.getWidth());
                map.put(item, s);
                layout.addChild(item);
            }
        }
    }

    public static class priorityAddTable extends Table {
        Table inner;
        fcSortf sortf;
        Runnable added;
        boolean unitSide;

        public priorityAddTable(fcSortf sortf) {
            this.sortf = sortf;
            added = () -> {};
            unitSide = true;

            setBackground(Tex.sliderBack);
            table(t -> inner = t).pad(4f).growX();

            rebuild();
        }

        public void added(Runnable run) {
            added = run;
        }

        public void setSide(boolean unit) {
            unitSide = unit;
            rebuild();
        }

        public void rebuild() {
            inner.clear();

            for (String name : fcSortf.sortfLst()) {
                if (sortf.has(name, unitSide)) continue;

                priorityAddItem item = new priorityAddItem(name);
                item.clicked(() -> {
                    sortf.add(name, unitSide);
                    added.run();
                    item.remove();
                });

                inner.add(item).row();
            }
        }
    }

    public static class priorityAddItem extends baseItem {
        String name;

        public priorityAddItem(String name) {
            this.name = name;
        }

        public void buildUI() {
            inner.add(fcSortf.localizedName(name)).wrap().grow();
        }
    }

    public static class addItem extends baseItem {
        Collapser col;

        public addItem(Collapser col) {
            this.col = col;
        }

        public void buildUI() {
            inner.add("+").center();
            canToggle = true;
            clicked(() -> col.setCollapsed(!holded));
        }

        public void reset() {
            if (holded) {
                clicked();
                col.setCollapsed(true);
            }
        }
    }

    public static abstract class baseItem extends Table {
        Table inner;
        boolean canToggle;
        boolean holded;

        public baseItem() {
            setBackground(Tex.sliderBack);
            table(t -> inner = t).pad(2f).growX();

            inner.addListener(new forwardEventListener(this));
            hovered(this::hovered);
            exited(this::exited);
            clicked(this::clicked);

            canToggle = false;
            buildUI();
            holded = false;
        }

        public void clicked() {
            holded = !holded;
            setColor(holded ? Pal.accent : Color.white);
        }

        public void hovered() {
            setColor(Pal.accent.cpy().a(0.7f));
        }

        public void exited() {
            setColor(holded ? Pal.accent : Color.white);
        }

        public abstract void buildUI();
    }

    public static class priorityItem extends Table {
        Table inner;
        baseSortf<?> item;
        dragLayout layout;
        boolean config;
        Collapser configCol;
        priorityTable table;

        @SuppressWarnings("unchecked")
        public priorityItem(dragLayout layout, baseSortf<?> item, priorityTable table) {
            this.item = item;
            this.layout = layout;
            this.table = table;
            config = false;
            setBackground(Tex.sliderBack);
            table(t -> inner = t).pad(2f).growX();
            
            if (item.hasConfig()) {
                priorityItemConfig<?> configTable = null;

                if (item.configType().equals(Category.class)) {
                    configTable = new cCategory((baseSortf<Category>) item);
                }

                if (configTable != null) {
                    configCol = new Collapser(configTable, true);
                    configTable.changed(this::change);
                }
            }

            clicked(this::toggleConfig);
            hovered(this::hovered);
            exited(this::exited);

            rebuild();
        }

        public void toggleConfig() {
            if (configCol == null) return;
            config = !config;
            setColor(config ? Pal.accent : Color.white);
            configCol.toggle();
        }

        public void hovered() {
            if (!config) setColor(Pal.accent.cpy().a(0.7f));
        }

        public void exited() {
            setColor(config ? Pal.accent : Color.white);
        }

        public void delete() {
            table.current.remove(item);
            parent.change();
        }
        
        public void rebuild() {
            inner.clear();
            
            inner.add(item.localizedName).width(100f).wrap().growY().left();
            inner.image(Icon.flipY).size(32f).scaling(Scaling.fit).right().get().addListener(new dragHandle(this, layout));
            inner.image(Icon.trash).size(32f).scaling(Scaling.fit).padRight(4f).right().get().clicked(this::delete);

            if (configCol != null) {
                inner.row();
                inner.add(configCol).colspan(2).center();
            }
        }
    }

    public static abstract class priorityItemConfig<T> extends Table {
        baseSortf<T> item;

        public priorityItemConfig(baseSortf<T> item, String name) {
            this.item = item;
            add(bundle.get("setMode.feature.setting.targetingPriority.config." + name, name)).wrap().grow().padTop(8f).padBottom(8f).row();
            buildUI();
        }

        public abstract void buildUI();
    }

    public static class cCategory extends priorityItemConfig<Category> {
        public cCategory(baseSortf<Category> item) {
            super(item, "cCategory");
        }

        public void buildUI() {
            contentSelecter selecter = new contentSelecter();
            selecter.col(4);
            selecter.minSelectedCount(1);
            for (Category cat : Category.all) selecter.add(Vars.ui.getIcon(cat.name()), cat.name());
            selecter.setSelected(item.config.name());
            selecter.changed(() -> {
                String name = selecter.getSelectedName();
                if (name == null) return;
                item.config = Category.valueOf(name);
                cCategory.this.change();
            });
            add(selecter).center();
        }
    }

    public static class checkSettingTable extends Table {
        CheckBox check;

        public checkSettingTable(String name) {
            add(name).width(width / Scl.scl() * 0.6f).left().wrap().growY();
            check = new CheckBox("");
            check.changed(this::change);
            add(check).right();
        }

        public void setChecked(boolean v) {
            check.setChecked(v);
        }

        public boolean checked() {
            return check.isChecked();
        }
    }

    public static class presetsTable extends Table {
        Table inner;
        Seq<presetItem> items;
        presetItem currentItem;
        Runnable applied;
        Building[] targets;
        boolean added;

        public presetsTable(Building[] selected) {
            setBackground(Tex.sliderBack);
            table(t -> inner = t).pad(2f).growX();
            items = new Seq<>();
            targets = selected;
            currentItem = null;
            added = false;
            applied = () -> {};
            inner.changed(this::rebuild);
            rebuild();
        }

        public void applied(Runnable run) {
            applied = run;
        }

        public void rebuild() {
            inner.clear();
            currentItem = null;

            int c = buildTargetingPreset.size();
            for (int i=0; i<c; i++) {
                presetItem item = new presetItem(i);
                items.add(item);
                inner.add(item).growY().padBottom(4f).row();
                item.clicked(() -> {
                    if (currentItem != item) {
                        if (currentItem != null) currentItem.setSelected(false);
                        currentItem = item;
                        item.setSelected(true);
                    }
                });
                if (i + 1 == c && added) {
                    item.fireClick();
                    item.rename = true;
                    item.rebuild();
                    added = false;
                }
            }

            inner.table(butt -> {
                butt.button(Icon.save, () -> {
                    currentItem.save(targets[0]);
                }).size(46f).scaling(Scaling.fit).right().disabled(e -> currentItem == null || targets.length > 0);
                butt.button(Icon.ok, () -> {
                    for (Building building : targets) currentItem.apply(building);
                    applied.run();
                }).size(46f).scaling(Scaling.fit).padRight(4f).right().disabled(e -> currentItem == null);
                butt.button(Icon.add, () -> {
                    buildTargetingPreset.add();
                    added = true;
                    rebuild();
                }).size(46f).scaling(Scaling.fit).padRight(4f).right();
            }).growX();
        }
    }

    public static class presetItem extends Table {
        Table inner;
        boolean rename;
        boolean selected;
        String name;
        int id;
        byte[] priorityData;
        boolean preferBuilding, preferExtinguish;

        public presetItem(int id) {
            setBackground(Tex.sliderBack);
            table(t -> inner = t).pad(2f).growX();
            rename = false;
            selected = false;
            this.id = id;
            name = buildTargetingPreset.getName(id);
            byte[] data = buildTargetingPreset.getData(id);
            Reads reads = new Reads(new DataInputStream(new ByteArrayInputStream(data)));
            priorityData = TypeIO.readBytes(reads);
            preferBuilding = reads.bool();
            preferExtinguish = reads.bool();
            reads.close();
            hovered(this::hovered);
            exited(this::exited);
            rebuild();
        }

        public void setSelected(boolean v) {
            selected = v;
            setColor(v ? Pal.accent : Color.white);
        }

        public void hovered() {
            if (!selected) setColor(Pal.accent.cpy().a(0.7f));
        }

        public void exited() {
            setSelected(selected);
        }

        public void apply(Building building) {
            if (building instanceof IFcTurretBuild) {
                fcCall.setBuildingSortf(building, priorityData);
                fcCall.setTurretPreferBuildingTarget(building, preferBuilding);
            }
            if (building instanceof IFcLiquidTurretBuild) {
                fcCall.setTurretPreferExtinguish(building, preferExtinguish);
            }
        }

        public void save(Building building) {
            if (building instanceof IFcTurretBuild fcTurretBuild) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                Writes writes = new Writes(new DataOutputStream(stream));
                fcTurretBuild.fcSortf().write(writes);
                writes.bool(fcTurretBuild.fcPreferBuildingTarget());
                writes.bool(building instanceof IFcLiquidTurretBuild fcLiquidTurretBuild ? fcLiquidTurretBuild.fcPreferExtinguish() : true);
                writes.close();
                buildTargetingPreset.put(id, name, stream.toByteArray());
            }
        }

        public void delete() {
            buildTargetingPreset.remove(id);
            parent.change();
        }

        public void rename(String name) {
            buildTargetingPreset.rename(id, name);
            this.name = name;
        }

        public void rebuild() {
            inner.clear();

            if (rename) {
                TextField field = inner.field(name, t -> {}).grow().height(30f).valid(t -> (!t.trim().isEmpty() && !buildTargetingPreset.has(t)) || t.trim().equals(name)).get();
                field.keyDown(KeyCode.enter, () -> {
                    if (!field.isValid()) return;
                    rename(field.getText().trim());
                    rename = false;
                    rebuild();
                });
                field.keyDown(KeyCode.escape, () -> {
                    rename = false;
                    rebuild();
                });
                field.addListener(new FocusListener() {
                    @Override
                    public void keyboardFocusChanged(FocusEvent event, Element element, boolean focused) {
                        if (!focused) {
                            rename = false;
                            rebuild();
                        }
                    }
                });
                field.selectAll();
                field.requestKeyboard();
            } else {
                inner.add(name).left().width(100f).wrap().growY().padRight(8f).get().addListener(new forwardEventListener(this));
                inner.image(Icon.trash).size(32f).scaling(Scaling.fit).padRight(8f).get().clicked(this::delete);
                inner.image(Icon.pencil).size(32f).scaling(Scaling.fit).get().clicked(() -> {
                    fireClick();
                    rename = true;
                    rebuild();
                });
            }
        }
    }
}
