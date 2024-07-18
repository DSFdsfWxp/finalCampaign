package finalCampaign.patch.impl;

import android.opengl.GLSurfaceView.*;
import arc.*;
import arc.backend.android.*;
import arc.backend.android.surfaceview.*;
import arc.graphics.*;
import finalCampaign.launch.*;
import arc.util.*;

import javax.microedition.khronos.opengles.*;

import org.spongepowered.asm.mixin.*;

/**
 * (Modified from {@link arc.backend.android.AndroidGraphics})
 * <p>
 * An implementation of {@link Graphics} for Android.
 * @author mzechner
 */
@Mixin(AndroidGraphics.class)
public abstract class fcAndroidGraphics extends Graphics implements Renderer{

    /**
     * When {@link AndroidApplication#onPause()} call
     * {@link AndroidGraphics#pause()} they <b>MUST</b> enforce continuous rendering. If not, {@link #onDrawFrame(GL10)} will not
     * be called in the GLThread while {@link #pause()} is sleeping in the Android UI Thread which will cause the
     * {@link AndroidGraphics#pause} variable never be set to false. As a result, the {@link AndroidGraphics#pause()} method will
     * kill the current process to avoid ANR
     */
    @Shadow(remap = false)
    protected AndroidApplicationConfiguration config;
    @Shadow(remap = false)
    GLSurfaceView20 view;

    protected GLSurfaceView20 createGLSurfaceView(AndroidApplication application, final ResolutionStrategy resolutionStrategy){
        if(!checkGL20()) throw new ArcRuntimeException("Arc requires OpenGL ES 2.0");

        Gl.reset();
        EGLConfigChooser configChooser = getEglConfigChooser();
        GLSurfaceView20 view = new GLSurfaceView20(androidLauncher.thisActivityInstance, resolutionStrategy, config.useGL30 ? 3 : 2);
        if(configChooser != null)
            view.setEGLConfigChooser(configChooser);
        else
            view.setEGLConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil);
        view.setRenderer(this);
        return view;
    }

    @Shadow(remap = false)
    protected abstract EGLConfigChooser getEglConfigChooser();

    @Shadow(remap = false)
    protected abstract boolean checkGL20();
}