package finalCampaign.feature.featureClass.tuner;

import arc.struct.*;
import arc.util.*;
import arc.func.*;
import arc.scene.event.Touchable;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import finalCampaign.*;
import mindustry.*;
import mindustry.ui.dialogs.*;

public class tunerPane extends Table {
    private ObjectMap<String, TextButton> map;

    public tunerPane(Table parent) {
        parent.pane(this).width(600f).scrollX(false);
        map = new ObjectMap<>();
    }

    public void addItem(String name, boolean customizable, @Nullable Cons<customBuilder> cons, @Nullable ObjectMap<String, Object> customMap) {
        table(t -> {
            t.add(bundle.get("tuner." + name + ".name")).left().width(400f).wrap().growY();
            if (customizable) {
                customPane pane = new customPane(name, customMap);
    
                t.button(bundle.get("custom"), pane::show).right().width(75f).padRight(8f);
                cons.get(pane);
            }
            Runnable updateOnOff = () -> {
                map.get(name).setText(fTuner.isOn(name) ? bundle.get("on") : bundle.get("off"));
            };
            TextButton button = t.button("null", () -> {
                fTuner.set(name, !fTuner.isOn(name));
                updateOnOff.run();
            }).right().width(75f).get();
            map.put(name, button);
            fTuner.load(name);
            updateOnOff.run();
        }).width(600f).padTop(8f);
        row();
    }

    public interface customBuilder {
        public void checkSetting(String name, boolean def);
        public void sliderSetting(String name, float def, float max, float min, float step);
        public void textSetting(String name, String def, String hint, int maxLength, @Nullable Func<String, String> processor);
    }
    
    public class customPane extends BaseDialog implements customBuilder {
        private ObjectMap<String, Object> map;
        private Table content;
        private String superName;

        public customPane(String name, ObjectMap<String, Object> map) {
            super(bundle.get("tuner." + name + ".name"));
            this.map = map;
            superName = name;
            addCloseListener();
            addCloseButton();
            cont.pane((t) -> content = t).width(600f).scrollX(false);
            content.setWidth(600f);
        }
        
        public void checkSetting(String name, boolean def) {
            content.table(t -> {
                t.add(bundle.get("tuner." + superName + "." + name + ".name")).left().width(500f).wrap().growY();
                TextButton button = new TextButton("null");
                Runnable updateOnOff = () -> {
                    button.setText((boolean) map.get(name) ? bundle.get("on") : bundle.get("off"));
                };
                button.changed(() -> {
                    fTuner.setCustomValue(superName, name, !(boolean) map.get(name));
                    updateOnOff.run();
                });
                t.add(button).right().width(75f);
                fTuner.loadCustom(superName, name, def);
                updateOnOff.run();
            }).width(600f).padTop(8f);
            content.row();
        }

        public void sliderSetting(String name, float def, float max, float min, float step) {
            content.table(t -> {
                Slider slider = new Slider(min, max, step, false);

                Table flow = new Table();
                flow.add(bundle.get("tuner." + superName + "." + name + ".name")).left().grow().wrap();
                Label valueLabel = flow.add("null").right().get();
                flow.margin(3f, 33f, 3f, 33f);
                flow.touchable = Touchable.disabled;

                slider.changed(() -> {
                    float v = slider.getValue();
                    valueLabel.setText(String.format("%.3f", v).toString());
                    fTuner.setCustomValue(superName, name, v);
                });

                fTuner.loadCustom(superName, name, def);

                slider.setValue((float) map.get(name));
                slider.change();

                t.stack(slider, flow).width(600f);
            }).width(600f).padTop(8f);
            content.row();
        }

        public void textSetting(String name, String def, String hint, int maxLength, @Nullable Func<String, String> processor) {
            content.table(t -> {
                String title = bundle.get("tuner." + superName + "." + name + ".name");
                t.add(title).left().wrap().growY().width(500f);
                t.button(bundle.get("set"), () -> {
                    Vars.ui.showTextInput(title, hint, maxLength, def, str -> {
                        if (processor != null) str = processor.get(str);
                        fTuner.setCustomValue(superName, name, str);
                    });
                }).right().width(75f);
                fTuner.loadCustom(superName, name, def);
            }).width(600f).padTop(8f);
            content.row();
        }
    }
}
