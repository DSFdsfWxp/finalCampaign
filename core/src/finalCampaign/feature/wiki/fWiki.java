package finalCampaign.feature.wiki;

import arc.files.*;
import arc.scene.ui.*;
import arc.struct.*;
import finalCampaign.*;
import mindustry.*;
import mindustry.gen.*;

public class fWiki {
    public static boolean supported() {
        return !Vars.headless;
    }

    public static void init() {}
    public static void load() {}

    public static void setupWikiButton(String name, Button button) {
        button.addListener(new Tooltip(t -> t.background(Tex.button).add("[accent]" + bundle.get("wiki." + name + ".name", name) + "[]\n" + bundle.get("wiki." + name + ".short", bundle.get("wiki.noDetail")))));
        button.clicked(() -> {
            show(name);
        });
    }

    public static void show(String name) {
        Seq<String> paths = new Seq<>(name.split("\\."));
        Fi file = finalCampaign.thisModZip.child("fcWiki");
        paths.add(bundle.getLocaleString() + ".md");
        for (String child : paths)
            file = file.child(child);

        wikiDialog dialog = new wikiDialog(name);
        dialog.show(file.exists() ? file.readString() : bundle.get("wiki.noFound"));
    }
}
