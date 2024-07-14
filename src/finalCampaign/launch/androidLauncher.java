package finalCampaign.launch;

import java.lang.reflect.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.telephony.*;
import arc.*;
import arc.backend.android.*;
import arc.files.*;
import arc.scene.ui.layout.*;
import arc.util.*;

public class androidLauncher extends AndroidApplication {
    public static final int PERMISSION_REQUEST_CODE = 1;

    private ApplicationListener listener;
    private Method handlePermissionsResult;
    private ApplicationListener instance;
    private Fi mindustryCore;
    private Fi javaJar;

    boolean doubleScaleTablets = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shareCrashSender.setDefaultUncaughtExceptionHandler(new androidCrashSender());

        if(doubleScaleTablets && isTablet(this)){
            Scl.setAddition(0.5f);
        }

        Log.info("[finalCampaign] pre-main bootstrap");

        instance = new shareApplicationListener() {
            protected void handleCrash(Throwable e, String msg) {
                shareCrashSender.sender.log(e);
                Log.err(msg, e);
                System.exit(1);
            }

            protected shareClassLoader createClassLoader() {
                return new androidClassLoader(getCacheDir());
            }

            protected void beforeLaunch() {
                Fi dataDir = Core.settings.getDataDirectory();
                Fi modDir = dataDir.child("mods");

                mindustryCore = dataDir.child("game.jar");
                shareMixinService.mod = modDir.child("finalCampaign.jar");
                javaJar = dataDir.child("java.jar");

                if (androidVersionChecker.modNeedUpdate() && shareMixinService.mod.exists()) shareMixinService.mod.delete();
                if (androidVersionChecker.gameNeedUpdate() && mindustryCore.exists()) mindustryCore.delete();
                if (androidVersionChecker.javaNeedUpdate() && javaJar.exists()) javaJar.delete();
                
                if (!mindustryCore.exists()) {
                    Fi file = Core.files.internal("fcLaunch/game.jar");
                    if (!dataDir.exists()) dataDir.mkdirs();
                    file.copyTo(mindustryCore);
                }

                if (!shareMixinService.mod.exists()) {
                    Fi file = Core.files.internal("fcLaunch/mod.jar");
                    if (!modDir.exists()) modDir.mkdirs();
                    file.copyTo(shareMixinService.mod);
                }

                if (!javaJar.exists()) {
                    ZipFi mod = new ZipFi(shareMixinService.mod);
                    Fi javaJarSrc = mod.child("class").child("java.jar");
                    javaJarSrc.copyTo(javaJar);
                }
            }

            protected Fi[] getJar() {
                ((androidClassLoader) shareMixinService.getClassLoader()).createModClassLoader(androidLauncher.this);
                return new Fi[] {mindustryCore, javaJar, shareMixinService.mod};
            }

            protected ApplicationListener createApplicationListener() {
                try {
                    Class<?> androidClientLauncher = shareMixinService.getClassLoader().findClass("finalCampaign.launch.sideAndroidClientLauncher");
                    listener = (ApplicationListener) androidClientLauncher.getDeclaredConstructor(androidLauncher.class).newInstance(androidLauncher.this);

                    handlePermissionsResult = androidClientLauncher.getDeclaredMethod("handlePermissionsResult");
                    Method checkFiles = androidClientLauncher.getDeclaredMethod("checkFiles", Intent.class);

                    handlePermissionsResult.setAccessible(true);
                    checkFiles.setAccessible(true);

                    checkFiles.invoke(listener, getIntent());

                    return listener;
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        initialize(instance, new AndroidApplicationConfiguration() {{
            useImmersiveMode = true;
            hideStatusBar = true;
        }});

        try {
            //new external folder
            Fi data = Core.files.absolute(((Context)this).getExternalFilesDir(null).getAbsolutePath());
            Core.settings.setDataDirectory(data);

            //delete unused cache folder to free up space
            try {
                Fi cache = Core.settings.getDataDirectory().child("cache");
                if (cache.exists()) {
                    cache.deleteDirectory();
                }
            } catch(Throwable t) {
                Log.err("Failed to delete cached folder", t);
            }


            //move to internal storage if there's no file indicating that it moved
            if (!Core.files.local("files_moved").exists()) {
                Log.info("Moving files to external storage...");

                try {
                    //current local storage folder
                    Fi src = Core.files.absolute(Core.files.getLocalStoragePath());
                    for (Fi fi : src.list()) {
                        fi.copyTo(data);
                    }
                    //create marker
                    Core.files.local("files_moved").writeString("files moved to " + data);
                    Core.files.local("files_moved_103").writeString("files moved again");
                    Log.info("Files moved.");
                } catch(Throwable t) {
                    Log.err("Failed to move files!");
                    t.printStackTrace();
                }
            }
        } catch(Exception e) {
            //print log but don't crash
            Log.err(e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i : grantResults) {
                if(i != PackageManager.PERMISSION_GRANTED) return;
            }
            try {
                handlePermissionsResult.invoke(listener);
            } catch(Exception ignore) {}
        }
    }

    private boolean isTablet(Context context) {
        TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return manager != null && manager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE;
    }
}
