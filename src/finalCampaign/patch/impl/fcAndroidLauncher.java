package finalCampaign.patch.impl;

import android.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.os.Build.*;
import android.os.*;
import arc.*;
import arc.backend.android.*;
import arc.files.*;
import arc.func.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import dalvik.system.*;
import mindustry.*;
import mindustry.game.Saves.*;
import mindustry.io.*;
import mindustry.net.*;
import mindustry.ui.dialogs.*;
import mindustry.android.*;
import java.io.*;
import java.lang.Thread.*;
import java.util.*;
import org.spongepowered.asm.mixin.*;
import finalCampaign.launch.*;

import static mindustry.Vars.*;

@Mixin(AndroidLauncher.class)
public abstract class fcAndroidLauncher extends AndroidApplication{
    @Shadow(remap = false)
    boolean doubleScaleTablets;
    @Shadow(remap = false)
    FileChooser chooser;
    @Shadow(remap = false)
    Runnable permCallback;

    @Shadow(remap = false)
    private boolean isTablet(Context context) {return false;}

    @Shadow(remap = false)
    public abstract void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);

    @Override
    protected void onCreate(Bundle savedInstanceState){
        UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            CrashSender.log(error);

            //try to forward exception to system handler
            if(handler != null){
                handler.uncaughtException(thread, error);
            }else{
                Log.err(error);
                System.exit(1);
            }
        });

        if(doubleScaleTablets && isTablet(androidLauncher.thisActivityInstance)){
            Scl.setAddition(0.5f);
        }

        initialize(createClientLauncher(), new AndroidApplicationConfiguration(){{
            useImmersiveMode = true;
            hideStatusBar = true;
        }});
        checkFiles(androidLauncher.thisActivityInstance.getIntent());

        try{
            //new external folder
            Fi data = Core.files.absolute(androidLauncher.thisActivityInstance.getExternalFilesDir(null).getAbsolutePath());
            Core.settings.setDataDirectory(data);

            //delete unused cache folder to free up space
            try{
                Fi cache = Core.settings.getDataDirectory().child("cache");
                if(cache.exists()){
                    cache.deleteDirectory();
                }
            }catch(Throwable t){
                Log.err("Failed to delete cached folder", t);
            }


            //move to internal storage if there's no file indicating that it moved
            if(!Core.files.local("files_moved").exists()){
                Log.info("Moving files to external storage...");

                try{
                    //current local storage folder
                    Fi src = Core.files.absolute(Core.files.getLocalStoragePath());
                    for(Fi fi : src.list()){
                        fi.copyTo(data);
                    }
                    //create marker
                    Core.files.local("files_moved").writeString("files moved to " + data);
                    Core.files.local("files_moved_103").writeString("files moved again");
                    Log.info("Files moved.");
                }catch(Throwable t){
                    Log.err("Failed to move files!");
                    t.printStackTrace();
                }
            }
        }catch(Exception e){
            //print log but don't crash
            Log.err(e);
        }
    }

    private void checkFiles(Intent intent){
        try{
            Uri uri = intent.getData();
            if(uri != null){
                File myFile = null;
                String scheme = uri.getScheme();
                if(scheme.equals("file")){
                    String fileName = uri.getEncodedPath();
                    myFile = new File(fileName);
                }else if(!scheme.equals("content")){
                    //error
                    return;
                }
                boolean save = uri.getPath().endsWith(saveExtension);
                boolean map = uri.getPath().endsWith(mapExtension);
                InputStream inStream;
                if(myFile != null) inStream = new FileInputStream(myFile);
                else inStream = androidLauncher.thisActivityInstance.getContentResolver().openInputStream(uri);
                Core.app.post(() -> Core.app.post(() -> {
                    if(save){ //open save
                        System.out.println("Opening save.");
                        Fi file = Core.files.local("temp-save." + saveExtension);
                        file.write(inStream, false);
                        if(SaveIO.isSaveValid(file)){
                            try{
                                SaveSlot slot = control.saves.importSave(file);
                                ui.load.runLoadSave(slot);
                            }catch(IOException e){
                                ui.showException("@save.import.fail", e);
                            }
                        }else{
                            ui.showErrorMessage("@save.import.invalid");
                        }
                    }else if(map){ //open map
                        Fi file = Core.files.local("temp-map." + mapExtension);
                        file.write(inStream, false);
                        Core.app.post(() -> {
                            System.out.println("Opening map.");
                            if(!ui.editor.isShown()){
                                ui.editor.show();
                            }
                            ui.editor.beginEditMap(file);
                        });
                    }
                }));
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private DexClassLoader createDexClassLoader(Fi jar, ClassLoader parent) {
        return new DexClassLoader(jar.file().getPath(), getFilesDir().getPath(), null, parent){
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException{
                //check for loaded state
                Class<?> loadedClass = findLoadedClass(name);
                if(loadedClass == null){
                    try{
                        //try to load own class first
                        loadedClass = findClass(name);
                    }catch(ClassNotFoundException | NoClassDefFoundError e){
                        //use parent if not found
                        return parent.loadClass(name);
                    }
                }

                if(resolve){
                    resolveClass(loadedClass);
                }
                return loadedClass;
            }
        };
    }

    private Fi createFi(Uri uri) {
        return new Fi(uri.getPath()){
            @Override
            public InputStream read(){
                try{
                    return androidLauncher.thisActivityInstance.getContentResolver().openInputStream(uri);
                }catch(IOException e){
                    throw new ArcRuntimeException(e);
                }
            }

            @Override
            public OutputStream write(boolean append){
                try{
                    return androidLauncher.thisActivityInstance.getContentResolver().openOutputStream(uri);
                }catch(IOException e){
                    throw new ArcRuntimeException(e);
                }
            }
        };
    }

    private ClientLauncher createClientLauncher() {
        return new ClientLauncher(){

            @Override
            public void hide(){
                androidLauncher.thisActivityInstance.moveTaskToBack(true);
            }

            @Override
            public rhino.Context getScriptContext(){
                return AndroidRhinoContext.enter(androidLauncher.thisActivityInstance.getCacheDir());
            }

            @Override
            public void shareFile(Fi file){
            }

            @Override
            public ClassLoader loadJar(Fi jar, ClassLoader parent) throws Exception{
                try {
                    ClassLoader classLoader = shareMixinService.getClassLoader();
                    if (jar.absolutePath().equals(shareMixinService.mod.absolutePath())) return classLoader;
                } catch(Throwable ignore) {}

                return createDexClassLoader(jar, parent);
            }

            @Override
            public void showFileChooser(boolean open, String title, String extension, Cons<Fi> cons){
                showFileChooser(open, title, cons, extension);
            }

            void showFileChooser(boolean open, String title, Cons<Fi> cons, String... extensions){
                String extension = extensions[0];

                if(VERSION.SDK_INT >= VERSION_CODES.Q){
                    Intent intent = new Intent(open ? Intent.ACTION_OPEN_DOCUMENT : Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType(extension.equals("zip") && !open && extensions.length == 1 ? "application/zip" : "*/*");

                    addResultListener(i -> androidLauncher.thisActivityInstance.startActivityForResult(intent, i), (code, in) -> {
                        if(code == Activity.RESULT_OK && in != null && in.getData() != null){
                            Uri uri = in.getData();

                            if(uri.getPath().contains("(invalid)")) return;

                            Core.app.post(() -> Core.app.post(() -> cons.get(createFi(uri))));
                        }
                    });
                }else if(VERSION.SDK_INT >= VERSION_CODES.M && !(androidLauncher.thisActivityInstance.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                androidLauncher.thisActivityInstance.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){
                    chooser = new FileChooser(title, file -> Structs.contains(extensions, file.extension().toLowerCase()), open, file -> {
                        if(!open){
                            cons.get(file.parent().child(file.nameWithoutExtension() + "." + extension));
                        }else{
                            cons.get(file);
                        }
                    });

                    ArrayList<String> perms = new ArrayList<>();
                    if(androidLauncher.thisActivityInstance.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                    if(androidLauncher.thisActivityInstance.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        perms.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                    androidLauncher.thisActivityInstance.requestPermissions(perms.toArray(new String[0]), AndroidLauncher.PERMISSION_REQUEST_CODE);
                }else{
                    if(open){
                        new FileChooser(title, file -> Structs.contains(extensions, file.extension().toLowerCase()), true, cons).show();
                    }else{
                        super.showFileChooser(open, "@open", extension, cons);
                    }
                }
            }

            @Override
            public void showMultiFileChooser(Cons<Fi> cons, String... extensions){
                showFileChooser(true, "@open", cons, extensions);
            }

            @Override
            public void beginForceLandscape(){
                androidLauncher.thisActivityInstance.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }

            @Override
            public void endForceLandscape(){
                androidLauncher.thisActivityInstance.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            }

        };
    }
}