package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import finalCampaign.patch.*;
import mindustry.world.meta.*;

@Mixin(StatValues.class)
public class fcStatValues {
    
    @Overwrite(remap = false)
    public static StatValue number(float value, StatUnit unit, boolean merge) {
        return new IFcStatNumberValue() {
            public float value() {
                return value;
            }

            public StatUnit unit() {
                return unit;
            }

            public void display(Table table) {
                String l1 = (unit.icon == null ? "" : unit.icon + " ") + Strings.autoFixed(value, 2), l2 = (unit.space ? " " : "") + unit.localized();

                if(merge){
                    table.add(l1 + l2).left();
                }else{
                    table.add(l1).left();
                    table.add(l2).left();
                }
            }
        };
    }
}
