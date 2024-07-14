package finalCampaign.launch;

import arc.*;
import arc.files.*;
import arc.Files.*;
import arc.backend.sdl.*;
import arc.backend.sdl.jni.*;
import arc.util.*;
import arc.util.Log.*;

public class desktopLauncher extends shareApplicationListener {
    private static String[] args;
    private static ApplicationListener instance;

    private Fi mindustryCore;

    public static void main(String[] arg) {
        args = arg;
        instance = new desktopLauncher();

        Log.info("[finalCampaign] pre-main bootstrap");

        shareCrashSender.setDefaultUncaughtExceptionHandler(new desktopCrashSender());

        try {
            new SdlApplication(instance, new SdlConfig(){{
                title = "Mindustry";
                maximized = true;
                width = 900;
                height = 700;
                for(int i = 0; i < arg.length; i++){
                    if(arg[i].charAt(0) == '-'){
                        String name = arg[i].substring(1);
                        try{
                            switch(name){
                                case "width": width = Integer.parseInt(arg[i + 1]); break;
                                case "height": height = Integer.parseInt(arg[i + 1]); break;
                                case "gl3": gl30 = true; break;
                                case "antialias": samples = 16; break;
                                case "debug": Log.level = LogLevel.debug; break;
                                case "maximized": maximized = Boolean.parseBoolean(arg[i + 1]); break;
                            }
                        }catch(NumberFormatException number){
                            Log.warn("Invalid parameter number value.");
                        }
                    }
                }
                setWindowIcon(FileType.internal, "icons/icon_64.png");
            }});
        } catch (Exception e) {
            Log.err(e);
        }
    }

    protected void handleCrash(Throwable error, String desc) {
        shareCrashSender.sender.log(error);
        Log.err(error);
        SDL.SDL_ShowSimpleMessageBox(SDL.SDL_MESSAGEBOX_ERROR, "oh no", desc);
        Core.app.exit();
    }


    protected shareClassLoader createClassLoader() {
        return new desktopClassLoader();
    }

    protected void beforeLaunch() {
        shareMixinService.thisJar = new Fi(getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
        Fi path = shareMixinService.thisJar.parent();
        Fi configFi = path.child("fcConfig.bin");

        bothConfigUtil.config config = bothConfigUtil.read(configFi);
        mindustryCore = path.child(config.gameJarName);

        Core.settings.setAppName(config.appName);

        shareMixinService.mod = Core.settings.getDataDirectory().child("mods/").child(config.modName);
    }

    protected Fi[] getJar() {
        return new Fi[] {mindustryCore, shareMixinService.mod};
    }

    protected ApplicationListener createApplicationListener() {
        try {
            Class<?> desktopLauncher = shareMixinService.getClassLoader().findClass("mindustry.desktop.DesktopLauncher");
            ApplicationListener listener = (ApplicationListener) desktopLauncher.getDeclaredConstructors()[0].newInstance((Object) args);
            return listener;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
