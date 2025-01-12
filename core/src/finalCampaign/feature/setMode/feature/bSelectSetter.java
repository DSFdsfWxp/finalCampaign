package finalCampaign.feature.setMode.feature;

import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import finalCampaign.bundle.*;
import finalCampaign.feature.setMode.*;
import finalCampaign.map.*;
import finalCampaign.ui.*;
import finalCampaign.util.*;
import mindustry.gen.*;

public abstract class bSelectSetter<T> extends IFeature {
    private boolean sandboxOnly;
    protected bundleNS bundleNS;

    public bSelectSetter(String name, boolean sandboxOnly) {
        category = "setting";
        super.name = name;
        this.sandboxOnly = sandboxOnly;
    }

    public boolean isSupported(Building[] selected) {
        return sandboxOnly ? fcMap.sandbox() : true;
    }

    public void buildUI(Building[] selected, Table table, bundleNS bundleNS) {
        this.bundleNS = bundleNS;
        
        T[] lst = valuesProvider();
        Seq<String> strLst = new Seq<>();
        for (T v : lst) strLst.add(transformer(v));

        boolean ambiguous = isAmbiguous(selected);
        fakeFinal<String> current = new fakeFinal<>(ambiguous ? "..." : transformer(currentValue(selected[0])));
        TextButton button = new TextButton(ambiguous ? "..." : current.get());

        table.left();
        table.add(bundleNS.get("name")).width(100f).left().wrap().growY();
        button.clicked(() -> {
            selectTable.showSelect(button, strLst.toArray(String.class), current.get(), s -> {
                button.setText(s);
                current.set(s);
                T val = lst[strLst.indexOf(s)];
                selected(selected, val);
            });
        });
        table.add(button).minWidth(75f).maxWidth(100f).expandX().right();
    }

    public abstract void selected(Building[] selected, T value);
    public abstract T[] valuesProvider();
    public abstract T currentValue(Building building);
    public abstract String transformer(T value);

    public boolean isAmbiguous(Building[] selected) {
        T value = currentValue(selected[0]);
        for (Building building : selected) if (!currentValue(building).equals(value)) return true;
        return false;
    }
}
