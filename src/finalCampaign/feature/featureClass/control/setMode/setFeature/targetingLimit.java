package finalCampaign.feature.featureClass.control.setMode.setFeature;

import java.io.*;
import arc.*;
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
import finalCampaign.ui.*;
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
        table.add(new limitTable(fcTurretBuild.fcFilter())).growX().pad(2f);
    }

    public static class limitTable extends Table {
        fcFilter filter;

        public limitTable(fcFilter filter) {
            this.filter = filter;

            for (String name : fcFilter.filterLst()) {
                add(new limitItem(name, filter)).growX().padBottom(4f).row();
            }

            changed(this::updateFilter);
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
            touchable = Touchable.enabled;
            col = new Collapser(new Table(), true);

            backgroundDarkness(0.5f);

            inner.add(fcFilter.localizedName(name)).wrap().grow().left().get();
            check = new CheckBox("");
            inner.add(check).padLeft(4f).right().row();
            inner.add(col).colspan(2).growX();

            hovered(this::hovered);
            exited(this::exited);
            check.changed(() -> {
                if (check.isChecked()) {
                    fFilter.add(name);
                } else {
                    fFilter.filters.remove(this.filter);
                    config = false;
                }
                update();
                parent.change();
            });
            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (event.targetActor.isDescendantOf(col) || event.targetActor.isDescendantOf(check)) return;
                    limitItem.this.toggleConfig();
                }
            });
            
            update();
        }

        public void toggleConfig() {
            if (col == null || filter == null) return;
            if (!filter.hasConfig()) return;
            col.toggle();
            config = !col.isCollapsed();
            setColor(config ? Pal.accent : Color.gray);
        }

        public void hovered() {
            if (!config) setColor(Color.gray);
        }

        public void exited() {
            if (!config) setColor(Color.darkGray);
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
                        col.setTable(c);
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
                            Core.app.post(cNum.this::rebuild);
                        }
                    }
                });
                field.selectAll();
                field.requestKeyboard();
            } else {
                inner.add(Integer.toString(filter.config)).colspan(7).growX();
                inner.button(bundle.get("edit"), () -> {
                    editing = true;
                    Core.app.post(this::rebuild);
                }).colspan(3).growX().get();
            }
        }
    }

    public abstract static class limitConfig<T> extends Table {
        baseFilter<T> filter;

        public limitConfig(baseFilter<T> filter, String name) {
            this.filter = filter;
            fillParent = true;

            add(bundle.get("setMode.feature.setting.targetingLimit.config." + name, name)).wrap().grow().left().padTop(8f).padBottom(8f).row();
            buildUI();
        }

        public abstract void buildUI();
    }
}
