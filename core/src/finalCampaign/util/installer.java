package finalCampaign.util;

import java.io.*;
import arc.*;
import arc.files.*;
import arc.util.*;
import finalCampaign.*;
import finalCampaign.launch.bothConfigUtil.*;
import finalCampaign.launch.*;
import mindustry.*;

public class installer {
    public static void install() throws Exception {
        if (Vars.ios) throw new RuntimeException("Wait...Java mod on ios? How can this happened?");

        if (Vars.mobile) {
            throw new RuntimeException("Mixins are not available on Android.");
        } else {
            installDesktop();
        }
    }

    public static boolean inInstalledGame() {
        try {
            Class.forName("finalCampaign.launch.shareLogger", true, Thread.currentThread().getContextClassLoader());
        } catch(Exception e) {
            return false;
        }

        return true;
    }

    private static void installDesktop() throws Exception {
        Fi gameJar = new Fi(new File(mindustry.Vars.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        if (!gameJar.absolutePath().toLowerCase().endsWith(".jar"))
            throw new RuntimeException("Could not locate mindustry jar file form class path.");
        
        Fi path = gameJar.parent();
        Fi configFi = path.child("fcConfig.properties");
        Fi scriptFile = path.child("fcLaunch." + (OS.isWindows ? "bat" : "sh"));
        Fi steamConfig = path.parent().child("Mindustry.json");
        Fi steamConfigOriginal = path.parent().child("Mindustry.json.original");
        Fi steamConfigFc = path.parent().child("Mindustry.json.fc");

        bothVersionControl.init(inInstalledGame());
        String originalLauncherVersion = bothVersionControl.currentLauncherVersion();
        bothVersionControl.install(finalCampaign.thisMod.file.file(), version.toVersionString());

        String script = "";

        if (Vars.steam) {
            if (!steamConfigOriginal.exists())
                steamConfig.copyTo(steamConfigOriginal);

            String steamConfigJson = steamConfig.readString();
            String originalLauncherPath = "jre/finalCampaign/launcher/" + originalLauncherVersion + "/launcher.jar";
            String nowLauncherPath = "jre/finalCampaign/launcher/" + bothVersionControl.currentLauncherVersion() + "/launcher.jar";

            steamConfigJson = steamConfigJson.replace("mindustry.desktop.DesktopLauncher", "finalCampaign.launch.desktopLauncher");
            steamConfigJson = steamConfigJson.replace("jre/desktop.jar", nowLauncherPath);
            steamConfigJson = steamConfigJson.replace(originalLauncherPath, nowLauncherPath);

            steamConfigFc.writeString(steamConfigJson);

            script = """
@echo off
setlocal
title finalCampaign Mod - Mindustry
:START

cls
echo Steam Mindustry Version Switcher
echo.
echo Witch version do you want to switch to?
echo [o] Original
echo [f] FinalCampaign
echo.
set /p answer="Your choice: "

if /i "%answer%"=="o" goto ORI
if /i "%answer%"=="f" goto FC

echo Invalid input, please try again.
pause
goto START

:FC
copy /Y ..\\Mindustry.json.fc ..\\Mindustry.json > nul
goto END

:ORI
copy /Y ..\\Mindustry.json.original ..\\Mindustry.json > nul
goto END

:END
echo.
echo Done.
echo Press any key to start the game. Or you can just close the window.
pause
cd ..
Mindustry %*
endlocal
                """;
            if (!OS.isWindows) 
                    script = """
#!/bin/bash

switch_version() {
    case $1 in
        o|O)
            cp -f ../Mindustry.json.original ../Mindustry.json
            ;;
        f|F)
            cp -f ../Mindustry.json.fc ../Mindustry.json
            ;;
        *)
            echo "Invalid input, please try again."
            sleep 2
            start_menu
            ;;
    esac
}

start_menu() {
    clear
    echo "Steam Mindustry Version Switcher"
    echo
    echo "Which version do you want to switch to?"
    echo "[o] Original"
    echo "[f] FinalCampaign"
    echo
    read -p "Your choice: " answer
    switch_version "$answer"
}

end_script() {
    echo
    echo "Done."
    echo "Press any key to start the game. Or you can just close the terminal."
    read -n 1 -s -r
    cd ..
    ./Mindustry "$@"
}

start_menu
end_script
                        """;
        } else {
            script = """
@echo off
title finalCampaign Mod - Mindustry
setlocal
set /p ver=<finalCampaign/launcher/current
java -jar ./finalCampaign/launcher/%ver%/launcher.jar %*
if %ERRORLEVEL% EQU 0 goto f
echo.
echo The game crash or your java is not installed correctly.
:f
pause
endlocal
                """;
            if (!OS.isWindows) 
                    script = """
#!/bin/sh
ver=`cat ./finalCampaign/launcher/current`
java -jar ./finalCampaign/launcher/$ver/launcher.jar "$@"
                        """;
        }

        if (scriptFile.exists())
            scriptFile.delete();
        scriptFile.writeString(script);

        config configSrc = new config();
        configSrc.gameJarName = gameJar.name();
        configSrc.dataDir = Core.settings.getDataDirectory().absolutePath();
        configSrc.isServer = Vars.headless;
        bothConfigUtil.write(configSrc, configFi.writer(false));

        Log.info("installer: done.");

        if (!Vars.headless) {
            Vars.ui.showOkText(bundle.get("info"), String.format(bundle.get(Vars.steam ? "installer.finishHintSteam" : "installer.finishHint"), scriptFile.absolutePath()), () -> {});
        } else {
            Log.info("[finalCampaign] " + bundle.get("info") + ": " + String.format(bundle.get("installer.finishHint"), scriptFile.absolutePath()));
        }
    }
}
