package finalCampaign.feature.featureClass.control.setMode.setFeature;

import arc.scene.ui.layout.*;
import arc.struct.*;
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
        table.add(choicesTable).growX();
    }

    public static class choicesTable extends pane {
        choiceItem selected;

        public choicesTable(IFcDrillBuild target) {
            selected = null;

            ObjectIntMap<Item> outs = target.fcScanOutput();
            int count = 0;
            for (Item item : outs.keys()) {
                int amount = outs.get(item);
                choiceItem cItem = new choiceItem(item, amount, target.fcCalcDrillSpeed(item, amount));

                if (count == 0) cItem.setSelected(true);
                cItem.selectedChanged(() -> {
                    if (!cItem.selected()) return;
                    if (selected != null) selected.setSelected(false);
                    selected = cItem;

                    fcCall.setDrillBuildingPreferItem((Building) target, item);
                });

                inner.add(cItem);
                if (++ count % 2 == 0) inner.row();
            }
        }
    }

    public static class choiceItem extends pane {
        public choiceItem(Item item, int amount, float speed) {
            addHoveredListener();
            clicked(this::toggleSelected);
            Block block = Vars.content.block("ore-" + item.name);
            ItemImage image = new ItemImage(block == null ? item.uiIcon : block.uiIcon, amount);
            inner.add(image).left();
            inner.add(Double.toString(Math.floor(speed / 60 * 100) / 100d) + "/s").padLeft(8f).wrap().growY().right();
        }
    }
}
