package finalCampaign.feature.featureClass.control.setMode.setFeature;

import java.io.*;
import arc.graphics.*;
import arc.input.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import finalCampaign.*;
import finalCampaign.feature.featureClass.buildTargetingLimit.*;
import finalCampaign.feature.featureClass.buildTargetingLimit.fcFilter.*;
import finalCampaign.net.*;
import finalCampaign.patch.*;
import finalCampaign.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class targetingLimit extends bAttributeSetter {
    public targetingLimit() {
        super("targetingLimit", "set", false);
        supportMultiSelect = false;
    }

    public boolean init(Building[] selected) {
        return selected[0] instanceof TurretBuild;
    }

    public void buildUI(Building[] selected, Table table) {
        IFcTurretBuild fcTurretBuild = (IFcTurretBuild) selected[0];
        table.add(new limitTable(fcTurretBuild.fcFilter()));
    }

    public static class limitTable extends pane {
        fcFilter filter;

        public limitTable(fcFilter filter) {
            this.filter = filter;

            for (String name : fcFilter.filterLst()) {
                inner.add(new limitItem(name, filter)).growX().row();
            }

            inner.changed(this::updateFilter);
        }

        public void updateFilter() {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Writes writes = new Writes(new DataOutputStream(stream));
            filter.write(writes);
            writes.close();
            fcCall.setBuildingFilter(filter.build, stream.toByteArray());
        }
    }

    public static class limitItem extends pane {
        String name;
        fcFilter fFilter;
        baseFilter<?> filter;
        boolean config;
        CheckBox check;
        Collapser col;
        
        public limitItem(String name, fcFilter filter) {
            this.name = name;
            config = false;
            fFilter = filter;

            inner.add(fcFilter.localizedName(name)).width(100f).wrap().growY().left().get().addListener(new forwardEventListener(this));
            check = new CheckBox("");
            inner.add(check).right();

            hovered(this::hovered);
            exited(this::exited);
            check.changed(() -> {
                if (check.isChecked()) {
                    fFilter.add(name);
                } else {
                    fFilter.filters.remove(this.filter);
                }
                update();
                parent.change();
            });
            clicked(this::toggleConfig);
            update();
        }

        public void toggleConfig() {
            if (col == null || filter == null) return;
            if (!filter.hasConfig()) return;
            col.toggle();
            config = !config;
            setColor(config ? Pal.accent : Color.white);
        }

        public void hovered() {
            if (!config) setColor(Pal.accent.cpy().a(0.7f));
        }

        public void exited() {
            if (!config) setColor(Color.white);
        }

        @SuppressWarnings("unchecked")
        public void update() {
            if (fFilter.has(name)) {
                filter = fFilter.get(name);
                check.setChecked(true);

                if (filter.hasConfig()) {
                    Class<?> type = filter.configType();

                    limitConfig<?> c = null;
                    if (type.equals(Integer.class)) {
                        c = new cNum((baseFilter<Integer>) filter);
                    }

                    if (c != null) {
                        if (col == null) {
                            col = new Collapser(c, true);
                        } else {
                            col.setTable(c);
                        }
                        c.changed(this::change);
                    }
                }
            } else {
                if (col != null) col.setCollapsed(true);
                filter = null;
                check.setChecked(false);
            }
        }
    }

    public static class cNum extends limitConfig<Integer> {
        Table inner;
        boolean editing;

        public cNum(baseFilter<Integer> filter) {
            super(filter, "cNum");
            editing = false;
        }

        public void buildUI() {
            inner = table().growX().get();
            rebuild();
        }

        public void rebuild() {
            inner.clear();

            if (editing) {
                TextField field = inner.field(Integer.toString(filter.config), t -> {}).height(30f).growX().valid(t -> Strings.canParsePositiveInt(t)).get();
                field.keyDown(KeyCode.enter, () -> {
                    if (!field.isValid()) return;
                    int v = Integer.parseInt(field.getText());
                    boolean t = filter.config != v;
                    filter.config = v;
                    editing = false;
                    rebuild();
                    if (t) change();
                });
                field.keyDown(KeyCode.escape, () -> {
                    editing = false;
                    rebuild();
                });
                field.addListener(new FocusListener() {
                    @Override
                    public void keyboardFocusChanged(FocusEvent event, Element element, boolean focused) {
                        if (!focused) {
                            editing = false;
                            rebuild();
                        }
                    }
                });
                field.selectAll();
                field.requestKeyboard();
            } else {
                inner.add(Integer.toString(filter.config)).colspan(7).growX();
                inner.button(bundle.get("edit"), () -> {
                    editing = true;
                    rebuild();
                }).colspan(3).growX();
            }
        }
    }

    public abstract static class limitConfig<T> extends Table {
        baseFilter<T> filter;

        public limitConfig(baseFilter<T> filter, String name) {
            this.filter = filter;

            add(bundle.get("setMode.feature.setting.targetingLimit.config." + name, name)).wrap().grow().left().padTop(8f).padBottom(8f).row();
            buildUI();
        }

        public abstract void buildUI();
    }

    public abstract static class pane extends Table {
        Table inner;

        public pane() {
            setBackground(Tex.sliderBack);
            table(t -> inner = t).pad(4f).growX();
        }
    }
}
