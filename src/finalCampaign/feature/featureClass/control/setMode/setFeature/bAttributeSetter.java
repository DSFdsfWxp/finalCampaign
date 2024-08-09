package finalCampaign.feature.featureClass.control.setMode.setFeature;

import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import finalCampaign.bundle.*;
import finalCampaign.feature.featureClass.control.setMode.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;

public abstract class bAttributeSetter extends iFeature {
    protected bundleNS bundleNS;
    private String action;
    private boolean sandboxOnly;
    protected boolean background;

    public bAttributeSetter(String name, String action, boolean sandboxOnly) {
        category = "setting";
        this.name = "attributeSetter";
        bundleNS = new bundleNS("setMode.feature.setting." + name);
        this.action = action;
        this.sandboxOnly = sandboxOnly;
        background = true;
    }

    public boolean isSupported(Building[] selected) {
        if (sandboxOnly && Vars.state.rules.mode() != Gamemode.sandbox) return false;
        return init(selected);
    }

    public void buildUI(Building[] selected, Table table, bundleNS bundleNS) {
        table.add(this.bundleNS.get("name")).left().width(100f).wrap().growY();
        ButtonGroup<TextButton> group = new ButtonGroup<>();
        group.setMinCheckCount(0);
        TextButton button = new TextButton(bundleNS.get(action));
        table.add(button).width(50f).group(group).right().row();
        Collapser col = new Collapser(new Table(t -> {
            t.setWidth(Scl.scl(172f));
            if (background) t.setBackground(Tex.sliderBack);
            buildUI(selected, t);
        }), true);
        button.clicked(() -> col.toggle());
        table.add(col).center();
    }

    public abstract void buildUI(Building[] selected, Table table);
    public abstract boolean init(Building[] selected);
}
