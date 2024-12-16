package finalCampaign.feature.featureClass.wiki;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import finalCampaign.*;
import mindustry.graphics.*;
import mindustry.ui.dialogs.*;

public class wikiDialog extends BaseDialog {
    private static final float[] levelToScale = new float[] {
        -1f,
        2f,
        1.5f,
        1.17f,
        1f,
        0.83f,
        0.67f
    };

    public wikiDialog(String name) {
        super(bundle.get("wiki.title") + " - " + bundle.get("wiki." + name + ".name", name));
        addCloseButton();
        addCloseListener();
    }

    public void show(String data) {
        cont.clear();

        cont.pane(new Table(tt -> {
            tt.fillParent = true;
            tt.table(table -> {
                table.left();
                for (String line : data.split("\n")) {
                    String t = line.trim();
                    String tmp = "";
                    if (t.startsWith("#")) {
                        int level = t.split(" ")[0].length();
                        tmp = t.substring(level + 1);
                        table.add(tmp).left().color(Pal.accent).fontScale(levelToScale[level]).wrap().grow();
                    } else if (t.startsWith("![](")) {
                        tmp = t.substring(4, t.length() - 5);
                        Fi file = finalCampaign.thisModFi.child("fcWiki").child(tmp);
                        TextureRegion img = Core.atlas.find("error");
                        if (file.exists()) img = new TextureRegion(new Texture(new Pixmap(file)));
                        table.image(img).center().scaling(Scaling.fit).maxWidth(600f).expandX();
                    } else {
                        table.add(line).left().wrap().grow();
                    }
                    table.row();
                }
            }).width(600f);
        })).grow().fill();

        show();
    }
}
