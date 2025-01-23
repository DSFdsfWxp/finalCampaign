package finalCampaign.feature.pressingVisualHint;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import finalCampaign.event.*;
import finalCampaign.graphics.*;
import java.nio.*;

public class logic {
    private static ByteBuffer crosshairBuff, buff;

    public static void drawTop(fcDrawWorldTopEvent event) {
        if (!fPressingVisualHint.isOn())
            return;

        float scale = fPressingVisualHint.config.scale.value;
        int size = (int)(350 * scale);
        int borderSize = (int)(5 * scale);
        int fullSize = size + borderSize * 2;

        int cx = Mathf.clamp(Core.input.mouseX(), fullSize / 2, (int)(Core.scene.getWidth() - fullSize / 2f));
        int cy = Mathf.clamp(Core.input.mouseY(), fullSize / 2, (int)(Core.scene.getHeight() - fullSize / 2f));

        int mcy = cy > Core.scene.getHeight() - size / 2f - fullSize ? cy - fullSize : cy + fullSize;

        Draw.color(Color.black);
        Fill.rect(cx, mcy, fullSize, fullSize);
        Draw.color();

        if (buff == null)
            buff = Buffers.newByteBuffer(size * size * 4);

        int tx = cx - size / 2;
        int ty = mcy - size / 2;

        Core.gl.glReadPixels(tx, ty, size, size, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, buff);
        Pixmap pixmap = new Pixmap(buff, size, size);
        Texture texture = new Texture(pixmap);
        Draw.rect(new TextureRegion(texture), cx, mcy, size, size);
        pixmap.dispose();
        texture.dispose();

        int crossWidth = (int)(5 * scale);
        int crossHeight = (int)(30 * scale);
        ty = Core.input.mouseY() - cy + mcy;

        crosshairBuff = fcDrawf.crosshairWithReversedColor(Core.input.mouseX(), ty, crossWidth, crossHeight, 1f, crosshairBuff);
    }
}
