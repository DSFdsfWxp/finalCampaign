package finalCampaign.ui;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.core.*;
import mindustry.type.*;
import mindustry.ui.*;

public class itemImage extends Stack{

    public itemImage(TextureRegion region, int amount){

        add(new Table(o -> {
            o.left();
            o.add(new Image(region)).size(32f).scaling(Scaling.fit);
        }));

        if(amount != 0){
            add(new Table(t -> {
                t.left().bottom();
                t.add(amount >= 1000 ? UI.formatAmount(amount) : amount + "").style(Styles.outlineLabel);
                t.pack();
            }));
        }
    }

    public itemImage(TextureRegion region, Intp amount, Boolp infinity) {
        add(new Table(o -> {
            o.left();
            o.add(new Image(region)).size(32f).scaling(Scaling.fit);
        }));

        add(new Table(t -> {
            t.left().bottom();
            t.add("").style(Styles.outlineLabel).update(l -> {
                int num = amount.get();
                if (infinity.get()) {
                    l.setText("∞");
                } else if (num <= 0) {
                    l.setText("");
                } else {
                    l.setText(num >= 1000 ? UI.formatAmount(num) : num + "");
                }
            });
            t.pack();
        }));
    }

    public itemImage(ItemStack stack){
        this(stack.item.uiIcon, stack.amount);
    }

    public itemImage(PayloadStack stack){
        this(stack.item.uiIcon, stack.amount);
    }
}