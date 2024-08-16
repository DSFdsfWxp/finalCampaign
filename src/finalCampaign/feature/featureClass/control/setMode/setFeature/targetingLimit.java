package finalCampaign.feature.featureClass.control.setMode.setFeature;

import arc.*;
import arc.graphics.*;
import arc.input.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
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
                limitItem item = new limitItem(name, filter);
                add(item).growX().padBottom(4f).row();
                item.modified(this::updateFilter);
            }
        }

        public void updateFilter() {
            fcCall.setBuildingFilter(filter.build, filter.write());
        }
    }

    public static class limitItem extends pane {
        String name;
        fcFilter fFilter;
        baseFilter<?> filter;
        boolean config;
        CheckBox check;
        Collapser col;
        Seq<Runnable> modifiedListener;
        
        public limitItem(String name, fcFilter filter) {
            this.name = name;
            config = false;
            fFilter = filter;
            touchable = Touchable.enabled;
            col = new Collapser(new Table(), true);
            modifiedListener = new Seq<>();

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
                    fireModified();
                    fFilter.remove(name);
                } else {
                    fFilter.remove(name);
                    fireModified();
                    filter.filters.add(this.filter);
                }
            });
            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (event.targetActor.isDescendantOf(col) || event.targetActor.isDescendantOf(check)) return;
                    limitItem.this.toggleConfig();
                }
            });
            
            update(this::update);
        }

        public void modified(Runnable run) {
            if (!modifiedListener.contains(run)) modifiedListener.add(run);
        }

        public void fireModified() {
            for (Runnable run : modifiedListener) run.run();
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
                check.setChecked(true);

                baseFilter<?> n = fFilter.get(name);
                if (filter != null) if (filter.equals(n)) return;

                filter = n;

                if (filter.hasConfig()) {
                    Class<?> type = filter.configType();

                    limitConfig<?> c = null;
                    if (type.equals(Integer.class)) {
                        c = new cNum((baseFilter<Integer>) filter);
                    }

                    if (c != null) {
                        col.setTable(c);
                        c.modified(this::fireModified);
                    }
                }
            } else {
                if (col != null) if (!col.isCollapsed()) toggleConfig();
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
                    int original = filter.config;
                    filter.config = v;
                    if (t) fireModified();
                    filter.config = original;
                    editing = false;
                    rebuild();                    
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
                            if (!field.isValid()) return;
                            int v = Integer.parseInt(field.getText());
                            boolean t = filter.config != v;
                            int original = filter.config;
                            filter.config = v;
                            editing = false;
                            if (t) fireModified();
                            filter.config = original;
                            rebuild();
                        }
                    }
                });
                field.selectAll();
                field.requestKeyboard();
            } else {
                inner.add(Integer.toString(filter.config)).colspan(7).growX().update(l -> l.setText(Integer.toString(filter.config)));
                inner.button(bundle.get("edit"), () -> {
                    editing = true;
                    Core.app.post(this::rebuild);
                }).colspan(3).growX().get();
            }
        }
    }

    public abstract static class limitConfig<T> extends Table {
        baseFilter<T> filter;
        Seq<Runnable> modifiedListener;

        public limitConfig(baseFilter<T> filter, String name) {
            this.filter = filter;
            fillParent = true;
            modifiedListener = new Seq<>();

            add(bundle.get("setMode.feature.setting.targetingLimit.config." + name, name)).wrap().grow().left().padTop(8f).padBottom(8f).row();
            buildUI();
        }

        public void modified(Runnable run) {
            if (!modifiedListener.contains(run)) modifiedListener.add(run);
        }

        public void fireModified() {
            for (Runnable run : modifiedListener) run.run();
        }

        public abstract void buildUI();
    }
}
