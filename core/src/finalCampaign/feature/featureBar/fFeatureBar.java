package finalCampaign.feature.featureBar;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.struct.*;
import finalCampaign.*;
import finalCampaign.event.*;
import finalCampaign.feature.featureBar.shortcut.*;
import finalCampaign.feature.hudUI.*;
import finalCampaign.feature.tuner.*;
import finalCampaign.ui.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.ui.*;

public class fFeatureBar {

    protected static tunerConfig config;
    protected static boolean enabled;

    public static boolean supported() {
        return !Vars.headless;
    }

    public static void earlyInit() {
        config = new tunerConfig();
    }

    public static void lateLoad() {
        Events.on(EventType.StateChangeEvent.class, logic::stateChanged);
        ui.init();

        enabled = fTuner.add("featureBar", false, config, v -> {
            if (enabled == v)
                return;

            fHudUI.rebuildFixedLayer();
            if (v)
                logic.patchHudUI();
            else
                logic.restoreHudUI();

            enabled = v;
        });

        if (!enabled)
            logic.restoreHudUI();

        registerShortcuts();
        setDefaultOnBarButtons();
    }

    public static void earlyLoad() {
        Events.on(fcInputHandleBuildPlacementUIEvent.class, logic::buildPlacementUI);
    }

    public static void registerFetureButton(featureButton button) {
        ui.buttons.add(button);

        if (Vars.state != null && Vars.state.getState() != GameState.State.menu)
            ui.setup();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void buildUI() {
        Vars.control.setInput(Core.settings.getBool("keyboard", false) || !Vars.mobile ? new DesktopInput() : new MobileInput());

        if (enabled)
            ui.buildBarUI();
    }

    private static void registerShortcuts() {
        commandMode.register();
        coreDatabase.register();
        schematics.register();
        techTree.register();
    }

    private static void setDefaultOnBarButtons() {
        if (setting.getAndCast("featureBar.defaultOnBarButtons", false))
            return;

        Cons2<String, Integer> setShowIndexOnBar = (name, index) -> {
            setting.put("featureBar." + name + ".showOnBar", index);
        };

        if (Vars.mobile) {
            commandMode.button.setShowIndexOnBar(0);
            setShowIndexOnBar.get("setMode", 1);
            schematics.button.setShowIndexOnBar(2);
        } else {
            schematics.button.setShowIndexOnBar(0);
            coreDatabase.button.setShowIndexOnBar(1);
            setShowIndexOnBar.get("setMode", 2);
        }

        setting.put("featureBar.defaultOnBarButtons", true);
    }


    public static class tunerConfig {
        boolean rememberMoreWindowPosition = true;

        public void resetMoreWindowPosition() {
            fHudUI.windowLayer.resetWindowPosition(ui.moreWindow);
        }
    }

    public static class actionFeatureButton extends featureButton {

        private Runnable handle;

        public actionFeatureButton(Drawable icon, String name) {
            this(icon, name, () -> {});
        }

        public actionFeatureButton(Drawable icon, String name, Runnable handle) {
            super(icon, name, () -> true);
            this.handle = handle;
        }

        public void setHandle(Runnable handle) {
            this.handle = handle;
        }

        @Override
        protected void actionHandle(ImageButton button) {
            handle.run();
        }
    }

    public static class togglableFeatureButton extends featureButton {

        private Boolp toggleHandle;

        public togglableFeatureButton(Drawable icon, String name) {
            this(icon, name, () -> true);
        }

        public togglableFeatureButton(Drawable icon, String name, Boolp toggleHandle) {
            super(icon, name, () -> true);
            this.toggleHandle = toggleHandle;
        }

        public void setHandle(Boolp toggleHandle) {
            this.toggleHandle = toggleHandle;
        }

        @Override
        protected void actionHandle(ImageButton button) {
            if (toggleHandle.get())
                toggleChecked(false);
        }
    }

    public static class selectFeatureButton extends featureButton {

        private ObjectMap<String, selectionPair> selectionPairs;
        private String currentName;

        public selectFeatureButton(String name) {
            this(name, () -> true);
        }

        public selectFeatureButton(String name, Boolp valid) {
            super(Icon.cancel, name, valid);
            selectionPairs = new ObjectMap<>();
            currentName = null;
        }

        public void addSelection(Drawable icon, String name, Boolp handle) {
            selectionPair pair = new selectionPair();
            pair.icon = icon;
            pair.name = name;
            pair.handle = handle;

            selectionPairs.put(name, pair);
            if (currentName == null) {
                setIcon(icon);
                currentName = name;
            }
        }

        public selectionPair getCurrentSelection() {
            return selectionPairs.get(currentName);
        }

        public void setCurrentSelection(String name) {
            currentName = name;
            setIcon(selectionPairs.get(name).icon);
        }

        @Override
        protected void actionHandle(ImageButton button) {
            if (button == null)
                return;

            Seq<String> names = new Seq<>();
            for (String name : selectionPairs.keys())
                names.add("@featureBar." + this.getName() + "." + name + ".name");

            button.setChecked(true);

            selectTable.showSelect(button, names.toArray(String.class), "@featureBar." + this.getName() + "." + currentName + ".name", name -> {
                selectionPair pair = selectionPairs.get(name);
                if (pair.handle.get()) {
                    setIcon(pair.icon);
                    currentName = pair.name;
                    toolTipDesc = bundle.get(name);
                }

                button.setChecked(false);
            });
        }

        public static class selectionPair {
            public Drawable icon;
            public String name;
            public Boolp handle;
        }
    }

    public static abstract class featureButton {

        protected String toolTipDesc;

        private Drawable icon;
        private String name;
        private Boolp valid;
        private boolean checked;
        private int showIndexOnBar;

        public featureButton(Drawable icon, String name, Boolp valid) {
            this.icon = icon;
            this.name = name;
            this.valid = valid;

            checked = false;
            showIndexOnBar = setting.getAndCast("featureBar." + name + ".showOnBar", -1);
            toolTipDesc = "";
        }

        public String getName() {
            return name;
        }

        public Drawable getIcon() {
            return icon;
        }

        public boolean isValid() {
            return valid.get();
        }

        public boolean isChecked() {
            return checked;
        }

        public boolean isShownOnBar() {
            return showIndexOnBar != -1;
        }

        public int getShowIndexOnBar() {
            return showIndexOnBar;
        }

        public void setValid(Boolp valid) {
            this.valid = valid;
        }

        public void setShowIndexOnBar(int index) {
            if (showIndexOnBar == index)
                return;

            showIndexOnBar = index;
            setting.put("featureBar." + name + ".showOnBar", index);
        }

        public void setIcon(Drawable icon) {
            this.icon = icon;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public void toggleChecked(boolean callHandle) {
            setChecked(!checked);
            if (callHandle)
                actionHandle(null);
        }

        public void setState(Drawable currentIcon, boolean checked) {
            setIcon(icon);
            setChecked(checked);
        }

        public ImageButton buildButton() {
            ImageButton button = new featureImageButton(this, icon);

            button.update(() -> {
                button.getImage().setDrawable(icon);
                button.getImage().color.a(valid.get() ? 1f : 0.66f);
                button.setChecked(checked);
            });
            button.clicked(() -> {
                if (valid.get())
                    actionHandle(button);
            });
            button.addListener(new Tooltip(t -> {
                t.left();
                t.background(Tex.button);
                t.add(bundle.get("featureBar." + this.getName() + ".name")).update(l -> {
                    l.setColor(isValid() ? Pal.accent : Color.red);
                }).row();
                t.label(() -> valid.get() ? toolTipDesc : bundle.get("featureBar." + this.getName() + ".invalid", "featureBar.invalid")).color(Color.lightGray);
            }));

            return button;
        }

        protected abstract void actionHandle(ImageButton button);
    }

    public static class featureImageButton extends ImageButton {

        private featureButton button;

        public featureImageButton(featureButton button, Drawable icon) {
            this.button = button;

            getImage().setDrawable(icon);
            setStyle(Styles.clearTogglei);
        }

        public featureButton getFeatureButton() {
            return button;
        }
    }
}
