package finalCampaign.feature.featureClass.tuner;

import java.lang.annotation.*;
import java.lang.reflect.*;
import arc.struct.*;
import arc.util.*;
import arc.*;
import arc.graphics.*;
import arc.input.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import finalCampaign.*;
import finalCampaign.feature.featureClass.tuner.fTuner.*;
import mindustry.*;
import mindustry.ui.dialogs.*;

public class tunerPane extends Table {
    private ObjectMap<String, TextButton> map;

    public tunerPane(Table parent) {
        parent.pane(this).width(650f).scrollX(false);
        map = new ObjectMap<>();
    }

    public boolean addItem(String name, boolean customizable, boolean automatic, @Nullable Object config) {
        table(t -> {
            t.add(bundle.get("tuner." + name + ".name")).left().width(400f).wrap().growY();
            if (customizable) {
                configPane pane = new configPane(name, config);
                t.button(bundle.get("customize"), pane::show).right().width(75f).padRight(8f);
            }
            Runnable updateOnOff = () -> {
                if (automatic) {
                    map.get(name).setText(bundle.get("auto"));
                } else {
                    map.get(name).setText(fTuner.isOn(name) ? bundle.get("on") : bundle.get("off"));
                }
            };
            TextButton button = t.button("null", () -> {
                fTuner.set(name, !fTuner.isOn(name));
                updateOnOff.run();
            }).right().width(75f).get();
            button.setDisabled(automatic);
            map.put(name, button);
            fTuner.load(name);
            updateOnOff.run();
        }).width(600f).padTop(8f);
        row();
        return fTuner.isOn(name);
    }
    
    public class configPane extends BaseDialog {
        private Object config;
        private Table content;
        private String superName;

        public configPane(String name, Object config) {
            super(bundle.get("tuner." + name + ".name"));
            this.config = config;
            superName = name;
            addCloseListener();
            addCloseButton();
            rebuild();
        }

        public void rebuild() {
            cont.clear();

            cont.pane((t) -> content = t).width(650f).scrollX(false);
            content.setWidth(Scl.scl(600f));

            load(false);

            for (Field field : config.getClass().getDeclaredFields()) {
                Class<?> type = field.getType();
                String fieldName = field.getName();

                if (type.equals(boolean.class)) checkSetting(fieldName);
                if (type.equals(fTuner.floatSlider.class)) sliderSetting(fieldName);
                if (type.equals(fTuner.stringField.class)) textSetting(fieldName);
                if (type.equals(Color.class)) colorSetting(fieldName);
                if (type.equals(fTuner.uiPosition.class)) uiPositionSetting(fieldName);
            }

            content.button(bundle.get("reset"), () -> {
                load(true);
                rebuild();
            }).width(200f).padTop(13f);
        }

        public void load(boolean reset) {
            try {
                Object data = setting.getJson("tuner." + superName + ".config" + (reset ? ".default" : ""), config.getClass(), () -> null);

                if (data == null) {
                    if (!reset) setting.putJson("tuner." + superName + ".config.default", config);
                    return;
                }

                for (Field field : config.getClass().getDeclaredFields()) {
                    Object v = null;
                    try {
                        v = field.get(data);
                    } catch (Exception E) {
                        Log.err(E);
                    }
                    if (v == null) continue;

                    Class<?> type = field.getType();
                    boolean set = false;
                    for (Annotation a : type.getAnnotations()) if (a instanceof fTuner.setable) set = true;
                    if (set) {
                        Object t = field.get(config);
                        t.getClass().getDeclaredMethod("set", t.getClass()).invoke(t, v);
                    } else {
                        field.set(config, v);
                    }
                }

                if (reset) setting.remove("tuner." + superName + ".config");
            } catch(Exception e) {
                Log.err("Failed to load tuner config: " + superName, e);
            }
        }

        public void save() {
            setting.putJson("tuner." + superName + ".config", config);
        }

        public void checkSetting(String name) {
            content.table(t -> {
                t.add(bundle.get("tuner." + superName + "." + name + ".name")).left().width(500f).wrap().growY();
                TextButton button = new TextButton("null");
                Runnable updateOnOff = () -> {
                    button.setText((boolean) Reflect.get(config, name) ? bundle.get("on") : bundle.get("off"));
                };
                button.changed(() -> {
                    Reflect.set(config, name, !((boolean) Reflect.get(config, name)));
                    updateOnOff.run();
                    save();
                });
                t.add(button).right().width(75f);
                updateOnOff.run();
            }).width(600f).padTop(8f);
            content.row();
        }

        public void sliderSetting(String name) {
            fTuner.floatSlider src = Reflect.get(config, name);
            content.table(t -> {
                Slider slider = new Slider(src.min, src.max, src.step, false);

                Table flow = new Table();
                flow.add(bundle.get("tuner." + superName + "." + name + ".name")).left().grow().wrap();
                Label valueLabel = flow.add("null").right().get();
                flow.margin(3f, 33f, 3f, 33f);
                flow.touchable = Touchable.disabled;

                slider.changed(() -> {
                    float v = slider.getValue();
                    valueLabel.setText(String.format("%.3f", v).toString());
                    src.value = v;
                    save();
                });

                slider.setValue(src.value);
                slider.change();

                t.stack(slider, flow).width(600f);
            }).width(600f).padTop(8f);
            content.row();
        }

        public void textSetting(String name) {
            fTuner.stringField src = Reflect.get(config, name);
            content.table(t -> {
                String title = bundle.get("tuner." + superName + "." + name + ".name");
                t.add(title).left().wrap().growY().width(500f);
                t.button(bundle.get("set"), () -> {
                    Vars.ui.showTextInput(title, src.hint, src.maxLength, src.value, str -> {
                        if (src.processor != null) str = src.processor.get(str);
                        src.value = str;
                        save();
                    });
                }).right().width(75f);
            }).width(600f).padTop(8f);
            content.row();
        }

        public void colorSetting(String name) {
            content.table(t -> {
                ColorPicker picker = new ColorPicker();
                t.add(bundle.get("tuner." + superName + "." + name + ".name")).left().wrap().growY().width(350f);
                Label colorLabel = t.add("null").left().padLeft(8f).wrap().growY().width(142f).get();
                Runnable updateColorLabel = () -> {
                    String hex = ((Color) Reflect.get(config, name)).toString();
                    colorLabel.setText(hex);
                    colorLabel.setColor(Color.valueOf(hex));
                };
                t.button(bundle.get("set"), () -> {
                    picker.show(Reflect.get(config, name), c -> {
                        Reflect.set(config, name, c);
                        updateColorLabel.run();
                        save();
                    });
                }).right().width(75f);
                updateColorLabel.run();
            }).width(600f).padTop(8f);
            content.row();
        }

        public void uiPositionSetting(String name) {
            fTuner.uiPosition pos = Reflect.get(config, name);
            content.table(t -> {
                t.add(bundle.get("tuner." + superName + "." + name + ".name")).left().wrap().growY().width(500f);
                t.button(bundle.get("set"), () -> {
                    if (!Vars.state.isGame()) {
                        Vars.ui.showOkText(bundle.get("info"), bundle.get("tuner.pane.uiPosition.relatively.inGameHint"), () -> {});
                        return;
                    }

                    BaseDialog dialog = new BaseDialog("");
                    dialog.addCloseListener();
                    fTuner.uiPosition originalPosition = new uiPosition(pos);

                    Runnable closeAndSave = () -> {
                        originalPosition.set(pos);
                        dialog.hide();
                        save();
                    };

                    dialog.addListener(new InputListener() {
                        @Override
                        public boolean keyDown(InputEvent event, KeyCode keycode) {
                            if (keycode == KeyCode.enter) {
                                Vars.ui.showCustomConfirm(bundle.get("tuner.pane.uiPosition.relatively.title"), bundle.get("tuner.pane.uiPosition.relatively.question"), bundle.get("yes"), bundle.get("no"), () -> {
                                    pos.setRelatively(true);
                                    closeAndSave.run();
                                }, () -> {
                                    pos.setRelatively(false);
                                    closeAndSave.run();
                                });
                            }
                            return true;
                        }

                        @Override
                        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                            if (button == KeyCode.mouseLeft) pos.setAbsolute(x, y);
                            return true;
                        }

                        @Override
                        public void touchDragged(InputEvent event, float x, float y, int pointer) {
                            if (Core.input.keyDown(KeyCode.mouseLeft)) pos.setAbsolute(x, y);
                        }
                    });

                    dialog.hidden(() -> {
                        configPane.this.visible = true;
                        Vars.ui.settings.visible = true;
                        Vars.ui.paused.color.a = 1f;
                        pos.set(originalPosition);
                    });

                    dialog.show();
                    Vars.ui.showOkText(bundle.get("info"), bundle.get("tuner.pane.uiPosition.hint"), () -> {
                        dialog.color.a = 0f;
                        configPane.this.visible = false;
                        Vars.ui.settings.visible = false;
                        Vars.ui.paused.color.a = 0f;
                    });
                }).right().width(75f);
            }).width(600f).padTop(8f);
            content.row();
        }
    }
}
