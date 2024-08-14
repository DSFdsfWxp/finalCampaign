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
        supportMultiSelect = false;
    }

    public boolean init(Building[] selected) {
        return selected[0] instanceof IFcDrillBuild;
    }

    public void buildUI(Building[] selected, Table table) {
        IFcDrillBuild target = (IFcDrillBuild) selected[0];
        choicesTable choicesTable = new choicesTable(target);
        table.add(choicesTable).growX().pad(2f);
    }

    public static class choicesTable extends Table {
        choiceItem selected;

        public choicesTable(IFcDrillBuild target) {
            selected = null;
            left();

            ObjectIntMap<Item> outs = target.fcScanOutput();
            int count = 0;
            for (Item item : outs.keys()) {
                int amount = outs.get(item);
                choiceItem cItem = new choiceItem(item, amount, target.fcCalcDrillSpeed(item, amount));

                if (target.fcDrillTarget() == item) {
                    selected = cItem;
                    cItem.setSelected(true);
                }
                cItem.selectedChanged(() -> {
                    if (!cItem.selected()) return;
                    if (selected != null && selected != cItem) selected.setSelected(false);
                    selected = cItem;

                    fcCall.setDrillBuildingPreferItem((Building) target, item);
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
