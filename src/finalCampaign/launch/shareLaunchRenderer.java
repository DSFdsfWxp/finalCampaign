package finalCampaign.launch;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;

public class shareLaunchRenderer {
    private static Color backgroundColor;
    private static boolean done;
    private static boolean inited = false;

    private static TextureRegion logo;

    public static void init() throws Exception {
        backgroundColor = new Color(1f, 0.0431f, 0.0588f);
        Core.batch = new SortedSpriteBatch();
        done = false;
        inited = true;

        logo = new TextureRegion(new Texture(new Pixmap(shareIOUtil.readFileInternalAsByte("fcLaunch/logo.png"))));
    }

    public static boolean done() {
        return done;
    }

    public static void draw() {
        if (!inited) return;

        Core.graphics.clear(backgroundColor);

        final float w = Core.graphics.getWidth();
        final float h = Core.graphics.getHeight();

        Draw.proj().setOrtho(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());

        Draw.rect(logo, w / 2f, h * 0.8f - logo.height * (w * 0.5f / logo.width) / 2f, w * 0.5f, logo.height * (w * 0.5f / logo.width));

        Draw.flush();
        
        done = true;
    }
}
