package finalCampaign.launch;

import android.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.os.Build.*;
import arc.*;
import arc.files.*;
import arc.func.*;
import arc.util.*;
import dalvik.system.*;
import mindustry.*;
import mindustry.game.Saves.*;
import mindustry.io.*;
import mindustry.net.*;
import mindustry.ui.dialogs.*;

import java.io.*;
import java.lang.Thread.*;
import java.util.*;

import static mindustry.Vars.*;

public class sideAndroidClientLauncher extends ClientLauncher {
    public androidLauncher application;
    FileChooser chooser;
    Runnable permCallback;

    public sideAndroidClientLauncher(androidLauncher application) {
        this.application = application;
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
    }

    @Override
    public void hide(){
        application.moveTaskToBack(true);
    }

    @Override
    public rhino.Context getScriptContext(){
        try {
            Class<?> AndroidRhinoContext = Class.forName("mindustry.android.AndroidRhinoContext", true, getClass().getClassLoader());
            return (rhino.Context) AndroidRhinoContext.getDeclaredMethod("enter", File.class).invoke(null, application.getCacheDir());
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void shareFile(Fi file){
    }

    @Override
    public ClassLoader loadJar(Fi jar, ClassLoader parent) throws Exception{
        if (jar.absolutePath().equals(shareMixinService.mod.absolutePath())) return shareMixinService.getClassLoader();

        return new DexClassLoader(jar.file().getPath(), application.getFilesDir().getPath(), null, parent){
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

            application.addResultListener(i -> application.startActivityForResult(intent, i), (code, in) -> {
                if(code == Activity.RESULT_OK && in != null && in.getData() != null){
                    Uri uri = in.getData();

                    if(uri.getPath().contains("(invalid)")) return;

                    Core.app.post(() -> Core.app.post(() -> cons.get(new Fi(uri.getPath()){
                        @Override
                        public InputStream read(){
                            try{
                                return application.getContentResolver().openInputStream(uri);
                            }catch(IOException e){
                                throw new ArcRuntimeException(e);
                            }
                        }

                        @Override
                        public OutputStream write(boolean append){
                            try{
                                return application.getContentResolver().openOutputStream(uri);
                            }catch(IOException e){
                                throw new ArcRuntimeException(e);
                            }
                        }
                    })));
                }
            });
        }else if(VERSION.SDK_INT >= VERSION_CODES.M && !(application.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            application.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){
            chooser = new FileChooser(title, file -> Structs.contains(extensions, file.extension().toLowerCase()), open, file -> {
                if(!open){
                    cons.get(file.parent().child(file.nameWithoutExtension() + "." + extension));
                }else{
                    cons.get(file);
                }
            });

            ArrayList<String> perms = new ArrayList<>();
            if(application.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if(application.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                perms.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            application.requestPermissions(perms.toArray(new String[0]), androidLauncher.PERMISSION_REQUEST_CODE);
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
        application.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    @Override
    public void endForceLandscape(){
        application.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
    }

    protected void checkFiles(Intent intent){
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
                else inStream = application.getContentResolver().openInputStream(uri);
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

    protected void handlePermissionsResult() {
        if(chooser != null){
            Core.app.post(chooser::show);
        }
        if(permCallback != null){
            Core.app.post(permCallback);
            permCallback = null;
        }
    }

}
