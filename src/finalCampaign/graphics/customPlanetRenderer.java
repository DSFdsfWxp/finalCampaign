package finalCampaign.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;

public class customPlanetRenderer extends PlanetRenderer {
    @Override
    public void render(PlanetParams params) {
        customPlanetParams cParams = (customPlanetParams) params;

        Draw.flush();
        Gl.clear(Gl.depthBufferBit);
        Gl.enable(Gl.depthTest);
        Gl.depthMask(true);

        Gl.enable(Gl.cullFace);
        Gl.cullFace(Gl.back);

        int w = params.viewW <= 0 ? Core.graphics.getWidth() : params.viewW;
        int h = params.viewH <= 0 ? Core.graphics.getHeight() : params.viewH;

        bloom.blending = !params.drawSkybox;

        cam.up.set(Vec3.Y);

        cam.resize(w, h);
        params.camPos.setLength((params.planet.radius + params.planet.camRadius) * camLength + (params.zoom - 1f) * (params.planet.radius + params.planet.camRadius) * 2);

        if (params.otherCamPos != null) {
            cam.position.set(params.otherCamPos).lerp(params.planet.position, params.otherCamAlpha).add(params.camPos);
        } else {
            cam.position.set(params.planet.position).add(params.camPos);
        }

        cam.lookAt(cParams.lookAtPos == null ? params.planet.position : cParams.lookAtPos);
        cam.update();

        params.camUp.set(cam.up);
        params.camDir.set(cam.direction);

        projector.proj(cam.combined);
        batch.proj(cam.combined);

        Events.fire(Trigger.universeDrawBegin);

        bloom.resize(w, h);
        bloom.capture();

        if (params.drawSkybox) {

            Vec3 lastPos = Tmp.v31.set(cam.position);
            cam.position.setZero();
            cam.update();

            Gl.depthMask(false);

            skybox.render(cam.combined);

            Gl.depthMask(true);

            cam.position.set(lastPos);
            cam.update();
        }


        Events.fire(Trigger.universeDraw);

        Planet solarSystem = params.planet.solarSystem;
        renderPlanet(solarSystem, params);
        renderTransparent(solarSystem, params);

        bloom.render();

        Events.fire(Trigger.universeDrawEnd);

        Gl.enable(Gl.blend);

        if (params.renderer != null) {
            params.renderer.renderProjections(params.planet);
        }

        Gl.disable(Gl.cullFace);
        Gl.disable(Gl.depthTest);

        cam.update();
    }
}
