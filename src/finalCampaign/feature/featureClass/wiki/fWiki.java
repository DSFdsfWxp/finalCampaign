package finalCampaign.feature.featureClass.wiki;

import arc.files.*;
import arc.scene.ui.Button;
import arc.scene.ui.Tooltip;
import finalCampaign.*;
import mindustry.gen.Tex;

public class fWiki {

    public static void init() {}
    public static void load() {}

    public static void setupWikiButton(String name, Button button) {
        button.addListener(new Tooltip(t -> t.background(Tex.button).add("[accent]" + bundle.get("wiki." + name + ".name", name) + "[]\n" + bundle.get("wiki." + name + ".short", bundle.get("wiki.noDetail")))));
        button.clicked(() -> {
            show(name);
        });
    }

    public static void show(String name) {
        Fi file = finalCampaign.thisModFi.child("fcWiki").child(name.replace('.', '/') + ".md");
        wikiDialog dialog = new wikiDialog(name);
        dialog.show(file.readString());
    }
}
