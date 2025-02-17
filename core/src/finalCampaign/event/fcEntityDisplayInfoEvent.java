package finalCampaign.event;

import arc.scene.ui.layout.*;
import mindustry.gen.*;

public class fcEntityDisplayInfoEvent {
    public Building targetBuilding;
    public Unit targetUnit;
    public Table table;
    public boolean beforeDisplay;

    public void form(Building target, Table table, boolean before) {
        targetBuilding = target;
        targetUnit = null;
        this.table = table;
        beforeDisplay = before;
    }

    public void form(Unit target, Table table, boolean before) {
        targetUnit = target;
        targetBuilding = null;
        this.table = table;
        beforeDisplay = before;
    }
}
