package finalCampaign.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;
import java.nio.*;

public class fcDrawf {
    public static ByteBuffer crosshairWithReversedColor(float x, float y, float width, float height, float colorAlpha, @Nullable ByteBuffer buffToUse) {
        int cx = (int) x, cy = (int) y;
        int crossWidth = (int) width, crossHeight = (int) height;

        if (buffToUse == null)
            buffToUse = Buffers.newByteBuffer(crossWidth * crossHeight * 4);

        fcShaders.crosshair.a = colorAlpha;
        Draw.shader(fcShaders.crosshair);

        int tx = (int)(cx - crossHeight / 2f);
        int ty = (int)(cy - crossWidth / 2f);
        Core.gl.glReadPixels(tx, ty, crossHeight, crossWidth, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, buffToUse);
        Pixmap pixmap1 = new Pixmap(buffToUse, crossHeight, crossWidth);
        Texture texture1 = new Texture(pixmap1);
        Draw.rect(new TextureRegion(texture1), cx, cy, crossHeight, crossWidth);

        tx = (int)(cx - crossWidth / 2f);
        ty = (int)(cy - crossHeight / 2f);
        Core.gl.glReadPixels(tx, ty, crossWidth, crossHeight, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, buffToUse);
        Pixmap pixmap2 = new Pixmap(buffToUse, crossWidth, crossHeight);
        Texture texture2 = new Texture(pixmap2);
        Draw.rect(new TextureRegion(texture2), cx, cy, crossWidth, crossHeight);

        pixmap1.dispose();
        pixmap2.dispose();
        texture1.dispose();
        texture2.dispose();

        Draw.shader();

        return buffToUse;
    }
}
