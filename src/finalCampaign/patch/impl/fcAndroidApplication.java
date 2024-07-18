package finalCampaign.patch.impl;

import android.annotation.*;
import android.app.*;
import android.app.ActivityManager.AppTask;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import arc.Application;
import arc.*;
import arc.audio.*;
import arc.backend.android.*;
import arc.backend.android.AndroidApplication.*;
import arc.backend.android.surfaceview.*;
import arc.struct.*;
import arc.util.*;
import finalCampaign.launch.*;
import java.io.*;
import java.lang.reflect.*;
import java.lang.reflect.Proxy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * (Modified from {@link arc.backend.android.AndroidApplication})
 * <p>
 * An implementation of the {@link Application} interface for Android. Create an {@link Activity} that derives from this class. In
 * the {@link Activity#onCreate(Bundle)} method call the {@link #initialize(ApplicationListener)} method specifying the
 * configuration for the GLSurfaceView.
 * @author mzechner
 */
@SuppressWarnings({"deprecation", "unused"})
@Mixin(AndroidApplication.class)
public abstract class fcAndroidApplication extends Activity implements Application{

    @Shadow(remap = false)
    protected Seq<ApplicationListener> listeners;
    @Shadow(remap = false)
    protected Seq<Runnable> runnables;
    @Shadow(remap = false)
    protected Seq<Runnable> executedRunnables;
    @Shadow(remap = false)
    private IntMap<AndroidEventListener> eventListeners;
    @Shadow(remap = false)
    public Handler handler;
    @Shadow(remap = false)
    protected AndroidGraphics graphics;
    @Shadow(remap = false)
    protected AndroidInput input;
    @Shadow(remap = false)
    protected Audio audio;
    @Shadow(remap = false)
    protected AndroidFiles files;
    @Shadow(remap = false)
    protected Settings settings;
    @Shadow(remap = false)
    protected ClipboardManager clipboard;
    @Shadow(remap = false)
    protected boolean useImmersiveMode;
    @Shadow(remap = false)
    protected boolean hideStatusBar;

    private Activity proxyTarget;
    private InvocationHandler proxyHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ignore
    }

    private void init(ApplicationListener listener, AndroidApplicationConfiguration config, boolean isForView){
        if(this.getVersion() < AndroidApplication.MINIMUM_SDK){
            throw new ArcRuntimeException("Arc requires Android API Level " + AndroidApplication.MINIMUM_SDK + " or later.");
        }

        proxyTarget = androidLauncher.thisActivityInstance;
        androidLauncher.fcActivity = this;
        /*
        proxyHandle = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Class<?> declaringClass = method.getDeclaringClass();

                if (declaringClass.equals(AndroidApplication.class) || declaringClass.equals(Application.class) || declaringClass.equals(Disposable.class)) {
                    return method.invoke(fcAndroidApplication.this, args);
                }

                return method.invoke(proxyTarget, args);
            }
        };
        */

        Core.app = this;
        //Core.app = (Application) Proxy.newProxyInstance(shareMixinService.getClassLoader(), new Class[]{AndroidApplication.class, Activity.class, Application.class}, proxyHandle);

        graphics = new AndroidGraphics((AndroidApplication) Core.app, config, config.resolutionStrategy == null ? new FillResolutionStrategy() : config.resolutionStrategy);
        input = new AndroidInput((AndroidApplication) Core.app, proxyTarget, graphics.getView(), config);

        proxyTarget.getFilesDir(); // workaround for Android bug #10515463
        files = new AndroidFiles(proxyTarget.getAssets(), proxyTarget.getFilesDir().getAbsolutePath());
        settings = new Settings();
        addListener(listener);
        this.handler = new Handler();
        this.useImmersiveMode = config.useImmersiveMode;
        this.hideStatusBar = config.hideStatusBar;
        this.clipboard = (ClipboardManager) proxyTarget.getSystemService(Context.CLIPBOARD_SERVICE);

        //Core.app = this;
        Core.audio = audio = new Audio(!config.disableAudio);
        Core.settings = settings;
        Core.input = input;
        Core.files = files;
        Core.graphics = graphics;

        if(!isForView){
            try{
                proxyTarget.requestWindowFeature(Window.FEATURE_NO_TITLE);
            }catch(Exception ex){
                Log.err("[AndroidApplication] Content already displayed, cannot request FEATURE_NO_TITLE", ex);
            }
            proxyTarget.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            proxyTarget.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            proxyTarget.setContentView(graphics.getView(), createLayoutParams());
        }

        createWakeLock(config.useWakelock);
        hideStatusBar(this.hideStatusBar);
        useImmersiveMode(this.useImmersiveMode);
        if(this.useImmersiveMode && getVersion() >= Build.VERSION_CODES.KITKAT){
            try{
                View rootView = proxyTarget.getWindow().getDecorView();
                rootView.setOnSystemUiVisibilityChangeListener(arg0 -> this.handler.post(() -> useImmersiveMode(true)));
            }catch(Throwable e){
                Log.err("[AndroidApplication] Failed to create AndroidVisibilityListener", e);
            }
        }

        // detect an already connected bluetooth keyboardAvailable
        if(proxyTarget.getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS){
            Reflect.set(input, "keyboardAvailable", true);
        }
    }

    @Shadow(remap = false)
    protected abstract FrameLayout.LayoutParams createLayoutParams();

    protected void createWakeLock(boolean use){
        if(use){
            proxyTarget.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    protected void hideStatusBar(boolean hide){
        if(!hide) return;

        proxyTarget.getWindow().getDecorView().setSystemUiVisibility(0x1);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        //super.onWindowFocusChanged(hasFocus);
        useImmersiveMode(this.useImmersiveMode);
        hideStatusBar(this.hideStatusBar);
    }

    @TargetApi(19)
    public void useImmersiveMode(boolean use){
        if(!use || getVersion() < Build.VERSION_CODES.KITKAT) return;

        proxyTarget.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onPause(){
        input.onPause();

        if(isFinishing()){
            Reflect.invoke(graphics, "destroy");
        }else{
            Reflect.invoke(graphics, "pause");
        }

        //super.onPause();
    }

    @Override
    protected void onResume(){
        //Core.app = this;
        Core.settings = settings;
        Core.input = input;
        Core.audio = audio;
        Core.files = files;
        Core.graphics = graphics;

        input.onResume();
        Reflect.invoke(graphics, "resume");

        //super.onResume();
    }

    @Override
    protected void onDestroy(){
        //super.onDestroy();
        //force exit to reset statics and free resources
        System.exit(0);
    }

    @Override
    public boolean openFolder(String file){
        Log.info(file);
        Uri selectedUri = Uri.parse(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, "resource/folder");

        if(intent.resolveActivityInfo(proxyTarget.getPackageManager(), 0) != null){
            proxyTarget.startActivity(intent);
            return true;
        }else{
            proxyTarget.runOnUiThread(() -> {
                Toast.makeText(this, "Unable to open folder (missing valid file manager?)\n" + file, Toast.LENGTH_LONG).show();
            });
            return false;
        }
    }

    @Override
    public boolean openURI(String URI){
        try{
            proxyTarget.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URI)));
            return true;
        }catch(ActivityNotFoundException e){
            return false;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration config){
        //super.onConfigurationChanged(config);
        boolean keyboardAvailable = false;
        if(config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) keyboardAvailable = true;
        Reflect.set(input, "keyboardAvailable", keyboardAvailable);
    }

    @Override
    public void exit(){
        handler.post(Build.VERSION.SDK_INT < 21 ? proxyTarget::finish : proxyTarget::finishAndRemoveTask);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        //super.onActivityResult(requestCode, resultCode, data);

        // forward events to our listeners if there are any installed
        synchronized(eventListeners){
            if(eventListeners.containsKey(requestCode)){
                eventListeners.get(requestCode).onActivityResult(resultCode, data);
            }
        }

        if(data != null && data.getData() != null){
            String scheme = data.getData().getScheme();
            if(scheme.equals("file")){
                String fileName = data.getData().getEncodedPath();
                synchronized(listeners){
                    for(ApplicationListener list : listeners){
                        Core.app.post(() -> list.fileDropped(Core.files.absolute(fileName)));
                    }
                }
            }
        }
    }

    @Override
    public WindowManager getWindowManager() {
        return proxyTarget.getWindowManager();
    }

    @Override
    public PackageManager getPackageManager() {
        return proxyTarget.getPackageManager();
    }
}