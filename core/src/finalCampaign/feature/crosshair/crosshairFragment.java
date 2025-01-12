package finalCampaign.feature.crosshair;

import java.nio.*;
import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.util.*;
import finalCampaign.graphics.*;
import finalCampaign.ui.layout.*;
import finalCampaign.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.input.*;

public class crosshairFragment extends fragment {
    private boolean lastMoving, moving;

    public crosshairFragment(Group parent) {
        super(parent);
        lastMoving = true;
        moving = false;
    }

    public void checkMoving() {
        Unit unit = Vars.player.unit();
        moving = Vars.mobile ? unit!=null && unit.moving() : Core.input.axis(Binding.move_x) != 0 || Core.input.axis(Binding.move_y) != 0 || Core.input.keyDown(Binding.mouse_move);
    }

    @Override
    public void draw() {
        super.draw();
        if (!fCrosshair.isOn() || Vars.ui.minimapfrag.shown()) return;

        if ((moving || Vars.state.isPaused()) != lastMoving) {
            lastMoving = !lastMoving;

            if (lastMoving) {
                actions(Actions.alpha(fCrosshair.movingOpacity(), 0.4f));
            } else {
                actions(Actions.alpha(fCrosshair.staticOpacity(), 0.4f));
            }
        }

        final int dotHeight = (int)(4 * fCrosshair.scaleY());
        final int dotWidth = (int)(4 * fCrosshair.scaleX());
        final int crossHeight = (int)(4 * fCrosshair.scaleY());
        final int crossWidth = (int)(20 * fCrosshair.scaleX());
        final float cY = (layout.getSceneHeight() - this.y) / 2f;
        final float cX = (layout.getSceneWidth() - this.x) / 2f;
        
        if (fCrosshair.isInvertColor()) {
            shaders.crosshair.a = color.a;
            Draw.shader(shaders.crosshair);
        } else {
            Color customColor = fCrosshair.color();
            Draw.color(customColor.cpy().a(customColor.a * color.a));
        }

        Pixmap pixmap1 = null;
        Pixmap pixmap2 = null;
        Texture texture1 = null;
        Texture texture2 = null;

        if (fCrosshair.isInvertColor()) {
            if (fCrosshair.usingPoint()) {
                final int length = dotHeight * dotWidth * 4;
    
                int x = (int)(cX - dotWidth / 2);
                int y = (int)(cY - dotHeight / 2);
                ByteBuffer buff = Buffers.newByteBuffer(length);
                Core.gl.glReadPixels(x, y, dotWidth, dotHeight, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, buff);
                pixmap1 = new Pixmap(buff, dotWidth, dotHeight);
                texture1 = new Texture(pixmap1);
                Draw.rect(new TextureRegion(texture1), cX, cY, dotWidth, dotHeight);
            } else {
                final int length = crossHeight * crossWidth * 4;
    
                int x = (int)(cX - crossHeight / 2);
                int y = (int)(cY - crossWidth / 2);
                ByteBuffer buff = Buffers.newByteBuffer(length);
                Core.gl.glReadPixels(x, y, crossHeight, crossWidth, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, buff);
                pixmap1 = new Pixmap(buff, crossHeight, crossWidth);
                texture1 = new Texture(pixmap1);
                Draw.rect(new TextureRegion(texture1), cX, cY, crossHeight, crossWidth);
        
                x = (int)(cX - crossWidth / 2);
                y = (int)(cY - crossHeight / 2);
                buff = Buffers.newByteBuffer(length);
                Core.gl.glReadPixels(x, y, crossWidth, crossHeight, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, buff);
                pixmap2 = new Pixmap(buff, crossWidth, crossHeight);
                texture2 = new Texture(pixmap2);
                Draw.rect(new TextureRegion(texture2), cX, cY, crossWidth, crossHeight);
            }
        } else {
            if (fCrosshair.usingPoint()) {
                Fill.rect(cX, cY, dotWidth, dotHeight);
            } else {
                Fill.rect(cX, cY, crossHeight, crossWidth);
                Fill.rect(cX, cY, crossWidth, crossHeight);
            }
        }

        if (fCrosshair.isInvertColor()) {
            Draw.shader();

            if (texture1 != null) texture1.dispose();
            if (texture2 != null) texture2.dispose();
            if (pixmap1 != null) pixmap1.dispose();
            if (pixmap2 != null) pixmap1.dispose();
        } else {
            Draw.color();
        }
    }
}
