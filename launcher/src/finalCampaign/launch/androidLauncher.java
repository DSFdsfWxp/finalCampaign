package finalCampaign.launch;

import java.io.*;
import java.util.*;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.os.*;
import arc.util.*;

public class androidLauncher extends Activity {
    private AssetManager fcAssets;
    private shareLauncher fcInstance;
    private fi fcDataDir;
    private fi fcMindustryCore;
    private fi fcJavaJar;
    private fi fcAndroidJar;
    private fi fcPreMainJar;
    private boolean fcFirstOnCreate;

    public static Activity fcActivity;
    public static Activity thisActivityInstance;

    public androidLauncher() {
        thisActivityInstance = this;
        fcFirstOnCreate = true;
    }

    // proxies between this true activity to the fake activity (AndroidApplication in arc)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (fcActivity != null) Reflect.invoke(fcActivity, "onCreate", new Object[] {savedInstanceState}, Bundle.class);

        if (fcFirstOnCreate) main(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (fcActivity != null) Reflect.invoke(fcActivity, "onActivityResult", new Object[] {requestCode, resultCode, data}, int.class, int.class, Intent.class);
    }

    @Override
    public void onConfigurationChanged(Configuration config){
        super.onConfigurationChanged(config);
        if (fcActivity != null) fcActivity.onConfigurationChanged(config);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (fcActivity != null) Reflect.invoke(fcActivity, "onDestroy");
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (fcActivity != null) Reflect.invoke(fcActivity, "onResume");
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (fcActivity != null) Reflect.invoke(fcActivity, "onPause");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if (fcActivity != null) fcActivity.onWindowFocusChanged(hasFocus);
    }

    private void main(Bundle savedInstanceState) {
        files.ExternalStoragePath = "/sdcard/";
        try {
            File externalFileRootDir = getExternalFilesDir(null);
            do {
                externalFileRootDir = Objects.requireNonNull(externalFileRootDir).getParentFile();
            } while (Objects.requireNonNull(externalFileRootDir).getAbsolutePath().contains("/Android"));
            files.ExternalStoragePath = externalFileRootDir.getAbsolutePath() + "/";
        } catch(Throwable ignore) {}

        getFilesDir();
        files.LocalStoragePath = getExternalFilesDir(null).getAbsolutePath();
        if (!files.LocalStoragePath.endsWith("/")) files.LocalStoragePath += "/";
        
        fcAssets = getAssets();

        fcDataDir = new androidFi(fcAssets, getExternalFilesDir(null), fi.FileType.absolute);
        if (!fcDataDir.exists()) fcDataDir.mkdirs();

        files.instance = new files() {
            public fi internalFile(String path) {
                return new androidFi(fcAssets, path, fi.FileType.internal);
            }

            public fi dataDirectory() {
                return fcDataDir;
            }
        };

        shareCrashSender.setDefaultUncaughtExceptionHandler(new androidCrashSender());

        fcInstance = new shareLauncher() {
            protected void handleCrash(Throwable e, String msg) {
                shareCrashSender.sender.log(e);
                Log.err(msg, e);
                throw new RuntimeException(e);
            }

            protected shareClassLoader createClassLoader() {
                return new androidClassLoader(getCacheDir(), getCodeCacheDir(), androidLauncher.this.getApplicationInfo().nativeLibraryDir);
            }

            protected fi[] getJar() {
                ((androidClassLoader) shareMixinService.getClassLoader()).createModClassLoader(androidLauncher.this);
                return new fi[] {fcMindustryCore, fcJavaJar, fcPreMainJar, shareMixinService.mod, fcAndroidJar};
            }

            protected void launch() throws Exception {
                Class<?> main = shareMixinService.getClassLoader().findClass("finalCampaign.launch.sideAndroidMain");
                main.getDeclaredMethod("main", String.class, Activity.class, Bundle.class).invoke(null, fcDataDir.absolutePath(), androidLauncher.this, savedInstanceState);
            }
        };

        fcInstance.init();

        update();

        fcInstance.startup();
        fcFirstOnCreate = false;
    }

    private void update() {
        fi modDir = fcDataDir.child("mods");
        if (!modDir.exists()) modDir.mkdirs();

        shareMixinService.mod = modDir.child("finalCampaign.jar");
        fcMindustryCore = fcDataDir.child("game.jar");
        fcJavaJar = fcDataDir.child("java.jar");
        fcAndroidJar = fcDataDir.child("android.jar");
        fcPreMainJar = fcDataDir.child("preMain.jar");

        if (androidVersionChecker.modNeedUpdate() && shareMixinService.mod.exists()) shareMixinService.mod.delete();
        if (androidVersionChecker.checkNeedUpdate("game") && fcMindustryCore.exists()) fcMindustryCore.delete();
        if (androidVersionChecker.checkNeedUpdate("java") && fcJavaJar.exists()) fcJavaJar.delete();
        if (androidVersionChecker.checkNeedUpdate("android") && fcAndroidJar.exists()) fcAndroidJar.delete();
        if (androidVersionChecker.checkNeedUpdate("preMain") && fcPreMainJar.exists()) fcPreMainJar.delete();

        if (!shareMixinService.mod.exists()) {
            fi file = files.instance.internalFile("fcLaunch/mod.jar");
            file.copyTo(shareMixinService.mod);
            if (fcJavaJar.exists()) fcJavaJar.delete();
        }

        zipFi mod = new zipFi(shareMixinService.mod);

        if (!fcMindustryCore.exists()) {
            fi src = files.instance.internalFile("fcLaunch/game.jar");
            src.copyTo(fcMindustryCore);
            androidVersionChecker.registerCurrentVersion("game");
        }

        if (!fcJavaJar.exists()) {
            fi src = mod.child("class").child("java.jar");
            src.copyTo(fcJavaJar);
            androidVersionChecker.registerCurrentVersion("java");
        }

        if (!fcAndroidJar.exists()) {
            fi src = mod.child("class").child("android.jar");
            src.copyTo(fcAndroidJar);
            androidVersionChecker.registerCurrentVersion("android");
        }

        if (!fcPreMainJar.exists()) {
            fi src = files.instance.internalFile("fcLaunch/preMain.jar");
            src.copyTo(fcPreMainJar);
            androidVersionChecker.registerCurrentVersion("preMain");
        }
    }
}
