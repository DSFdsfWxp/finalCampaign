package finalCampaign.feature.featureClass.control.setMode.setFeature;

import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import finalCampaign.net.*;
import finalCampaign.patch.*;
import finalCampaign.ui.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;

public class drillPreferItem extends bAttributeSetter {
    public drillPreferItem() {
        super("drillPreferItem", "set", false);
        supportMultiSelect = true;
    }

    public boolean init(Building[] selected) {
        for (Building b : selected) if (b instanceof IFcDrillBuild) return true;
        return false;
    }

    public void buildUI(Building[] selected, Table table) {
        Seq<IFcDrillBuild> target = new Seq<>();
        for (Building b : selected) if (b instanceof IFcDrillBuild drillBuild) target.add(drillBuild);
        choicesTable choicesTable = new choicesTable(target);
        table.add(choicesTable).growX().pad(2f);
    }

    public static class choicesTable extends Table {
        choiceItem selected;

        public choicesTable(Seq<IFcDrillBuild> target) {
            selected = null;
            left();

            ObjectIntMap<Item> ores = new ObjectIntMap<>();
            ObjectFloatMap<Item> speeds = new ObjectFloatMap<>();

            for (IFcDrillBuild b : target) {
                ObjectIntMap<Item> outs = b.fcScanOutput();
                for (Item item : outs.keys()) {
                    ores.increment(item, 0, outs.get(item));
                    speeds.increment(item, 0, b.fcCalcDrillSpeed(item, outs.get(item)));
                }
            }

            Item current = target.get(0).fcDrillTarget();
            for (IFcDrillBuild b : target) {
                if (!b.fcDrillTarget().equals(current)) {
                    current = null;
                    break;
                }
            }

            int count = 0;
            for (Item item : ores.keys()) {
                int amount = ores.get(item);
                choiceItem cItem = new choiceItem(item, amount, speeds.get(item, 0f));

                if (current == item) {
                    selected = cItem;
                    cItem.setSelected(true);
                }
                cItem.selectedChanged(() -> {
                    if (!cItem.selected()) return;
                    if (selected != null && selected != cItem) selected.setSelected(false);
                    selected = cItem;

                    for(IFcDrillBuild b : target) {
                        if (!b.fcScanOutput().containsKey(item)) continue;
                        fcCall.setDrillBuildingPreferItem((Building) b, item);
                    }
                });

                add(cItem).growX().maxWidth(140f).left();
                if (++ count % 2 == 0) row();
            }
        }
    }

    public static class choiceItem extends pane {
        public choiceItem(Item item, int amount, float speed) {
            alwaysDrawBorder(false);
            touchable = Touchable.enabled;
            addListener(new HandCursorListener());
            backgroundDarkness(0.5f);
            clicked(() -> {
                setSelected(true);
                fireSelectedChanged();
            });
            Block block = Vars.content.block("ore-" + item.name);
            ItemImage image = new ItemImage(block == null ? item.uiIcon : block.uiIcon, amount);
            inner.add(image).left();
            inner.add(Double.toString(Math.floor(speed / 60f * 100f) / 100f) + "/s").padLeft(4f).wrap().grow().right().labelAlign(Align.right);
        }
    }
}
