package finalCampaign.feature.about;

import arc.graphics.*;
import arc.util.Reflect;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.Links.*;

public class fAbout {
    public static boolean supported() {
        return true;
    }

    public static void init() {}

    public static void load() {
        LinkEntry[] src = Links.getLinks();
        LinkEntry[] links = new LinkEntry[src.length + 1];
        System.arraycopy(src, 0, links, 0, src.length);
        links[links.length - 1] = new LinkEntry("finalCampaign.about", "finalCampaign://about", Icon.book, Color.valueOf("94c42a"));
        Reflect.set(Links.class, "links", links);
    }
}
