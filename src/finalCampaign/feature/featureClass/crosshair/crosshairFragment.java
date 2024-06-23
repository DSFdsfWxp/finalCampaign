package finalCampaign.feature.featureClass.crosshair;

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
import mindustry.core.GameState.*;
import mindustry.game.EventType.*;

public class crosshairFragment extends fragment {
    private boolean playerMoving;
    private float movingOpacity, staticOpacity;
    private float scaleX, scaleY;

    public crosshairFragment(Group parent) {
        super(null);
        playerMoving = true;

        Events.on(StateChangeEvent.class, e -> {
            if (e.to == State.playing) {
                movingOpacity = fCrosshair.movingOpacity();
                staticOpacity = fCrosshair.staticOpacity();
                scaleX = fCrosshair.scaleX();
                scaleY = fCrosshair.scaleY();
            }
        });
    }
    
    @Override
    public void draw() {
        super.draw();
        if (!fCrosshair.isOn()) return;

        Unit unit = Vars.player.unit();

        if ((unit.moving() || Vars.state.isPaused()) != playerMoving) {
            playerMoving = !playerMoving;

            if (playerMoving) {
                actions(Actions.alpha(movingOpacity, 30f));
            } else {
                actions(Actions.alpha(staticOpacity, 30f));
            }
        }

        final int dotHeight = (int)(4 * scaleY);
        final int dotWidth = (int)(4 * scaleX);
        final int crossHeight = (int)(4 * scaleY);
        final int crossWidth = (int)(20 * scaleX);
        final float cY = (layout.getSceneHeight() - this.y) / 2f;
        final float cX = (layout.getSceneWidth() - this.x) / 2f;
        
        shaders.crosshair.a = color.a;
        Draw.shader(shaders.crosshair);

        Pixmap pixmap1 = null;
        Pixmap pixmap2 = null;
        Texture texture1 = null;
        Texture texture2 = null;

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

        Draw.shader();

        if (texture1 != null) texture1.dispose();
        if (texture2 != null) texture2.dispose();
        if (pixmap1 != null) pixmap1.dispose();
        if (pixmap2 != null) pixmap1.dispose();
    }
}
