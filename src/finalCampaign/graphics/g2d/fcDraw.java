package finalCampaign.graphics.g2d;

import arc.graphics.g2d.*;

public class fcDraw {
    public static void rect(TextureRegion region, float x, float y, float w, float h) {
        Draw.rect(region, x + w / 2f, y + h / 2f, w, h);
    }
}
