package finalCampaign.launch;

import java.nio.charset.*;
import java.util.zip.*;
import arc.*;
import arc.files.*;
import arc.struct.*;
import arc.util.*;
import finalCampaign.*;
import finalCampaign.launch.bothConfigUtil.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.ui.dialogs.*;

public class installer {
    public static void install() throws Exception {
        if (Vars.ios) throw new RuntimeException("Wait...Java mod on ios? How can this happened?");

        if (Vars.mobile) {
            installAndroid();
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

    private static void installAndroid() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Object applicationInfo = Core.app.getClass().getMethod("getApplicationInfo").invoke(Core.app);
        Object packageManager = Core.app.getClass().getMethod("getPackageManager").invoke(Core.app);

        String packageName = (String) Core.app.getClass().getMethod("getPackageName").invoke(Core.app);
        String applicationName = (String) applicationInfo.getClass().getMethod("loadLabel", classLoader.loadClass("android.content.pm.PackageManager")).invoke(applicationInfo, packageManager);
        String className = Core.app.getClass().getName();
        String apkPath = (String) applicationInfo.getClass().getDeclaredField("sourceDir").get(applicationInfo);

        Fi tmpDir = finalCampaign.dataDir.child("installer");
        if (!tmpDir.exists()) tmpDir.mkdirs();

        ZipFi gameApk = new ZipFi(new Fi(apkPath));
        String gameApkHash = encoding.bytesToHex(encoding.sha256Hash((new Fi(apkPath)).readBytes()));

        Log.info(gameApkHash);

        Fi hash = tmpDir.child("hash");
        boolean gameApkNotModified = false;
        if (hash.exists()) gameApkNotModified = gameApkHash.equals(hash.readString());

        Seq<Fi> dexLst = new Seq<>();
        for (int i=1; true; i++) {
            String name = String.format("classes%s.dex", i == 1 ? "" : Integer.toString(i));
            Fi dex = gameApk.child(name);
            if (!dex.exists()) break;
            dexLst.add(dex);
        }

        dex2jar.init();
        Seq<Fi> jarLst = new Seq<>();

        for (Fi dex : dexLst) {
            byte[] bytecode = dex.readBytes();
            Fi outputFi = tmpDir.child("tmp." + dex.nameWithoutExtension() + ".jar");
            jarLst.add(outputFi);
            if (outputFi.exists()) {
                if (gameApkNotModified) continue;
                outputFi.deleteDirectory();
                outputFi.delete();
            }
            outputFi.mkdirs();
            dex2jar.from(bytecode).to(outputFi.file().toPath());
        }
        
        if (!gameApkNotModified) hash.writeString(gameApkHash);

        // simply pack the class we need for mixin
        Fi gameJar = tmpDir.child("game.jar");
        jarWriter writer = null;
        Seq<String> packLst = new Seq<>();
        packLst.addAll("mindustry", "arc", "com", "net", "rhino");

        if (!gameJar.exists() || !gameApkNotModified) {
            if (gameJar.exists()) gameJar.delete();
            writer = new jarWriter(gameJar, false);

            for (Fi jar : jarLst) {
                for (Fi file: jar.list()) if (packLst.indexOf(file.name()) > -1) {
                    if (file.isDirectory()) {
                        writer.add(file.name(), file);
                    } else {
                        writer.add(file.name(), file.readBytes());
                    }
                }
            }
    
            writer.close();
        }

        // convert the rest part to dex
        // maybe it's better to pack them too as android r8 may mix parts of mindustry into them
        Fi classFi = tmpDir.child("classes2.dex");
        if (!classFi.exists() || !gameApkNotModified) {
            if (classFi.exists()) classFi.delete();
            jar2dex jar2dex = new jar2dex();

            for (Fi jar : jarLst) {
                for (Fi file : jar.list()) if (packLst.indexOf(file.name()) == -1) {
                    if (file.isDirectory()) {
                        jar2dex.add(file.name(), file);
                    } else {
                        jar2dex.add(file.name(), file.readBytes());
                    }
                }
            }
    
            jar2dex.writeTo(classFi.write());
        }

        // patch AndroidManifest.xml for our new package name and activity name
        Fi manifest = gameApk.child("AndroidManifest.xml");
        xmlPatcher xPatcher = new xmlPatcher(manifest.readBytes());
        xPatcher.replaceString(packageName, packageName + ".finalCampaignMod");
        xPatcher.patchActivity(className, "finalCampaign.launch.androidLauncher");
        byte[] patchedManifest = xPatcher.build();

        // patch resource.arsc for new App name and package name
        Fi resources = gameApk.child("resources.arsc");
        arscPatcher aPatcher = new arscPatcher(resources.readBytes());
        aPatcher.replaceString(applicationName, "Fc " + applicationName);
        aPatcher.replacePackageName(packageName, packageName + ".finalCampaignMod");
        byte[] patchedResource = aPatcher.build();

        // build new apk
        Fi patchedApk = tmpDir.child("patched.apk");
        if (patchedApk.exists()) patchedApk.delete();
        writer = new jarWriter(patchedApk, true);

        // put our prepared files
        // include pre-main dex code, mixin configures and so on.
        Fi preMainJar = finalCampaign.thisModFi.child("class").child("preMain.android.jar");
        Fi preMainJarAsZip = tmpDir.child("preMain.jar");
        if (preMainJarAsZip.exists()) preMainJarAsZip.delete();
        preMainJar.copyTo(preMainJarAsZip);
        preMainJarAsZip = new ZipFi(preMainJarAsZip);

        Fi classDir = finalCampaign.thisModFi.child("class");

        writer.add("", preMainJarAsZip);
        writer.add("AndroidManifest.xml", patchedManifest);
        writer.add("resources.arsc", patchedResource, ZipOutputStream.STORED);
        writer.add("assets/fcLaunch/game.jar", gameJar.readBytes());
        writer.add("assets/fcLaunch/mod.jar", finalCampaign.thisMod.file.readBytes());
        writer.add("assets/fcLaunch/preMain.jar", classDir.child("preMain.android.src.jar").readBytes());
        if (classFi.exists()) writer.add("classes2.dex", classFi.readBytes());

        // these are for automatic updating mixin dependencies and our mod.
        writer.add("assets/fcLaunch/mod.version", String.format("%d.%d.%s", version.major, version.minor, version.type).getBytes(StandardCharsets.UTF_8));
        writer.add("assets/fcLaunch/game.sha256", encoding.sha256Hash(gameJar.readBytes()));
        writer.add("assets/fcLaunch/preMain.sha256", encoding.sha256Hash(classDir.child("preMain.android.src.jar").readBytes()));
        writer.add("assets/fcLaunch/java.sha256", encoding.sha256Hash(classDir.child("java.jar").readBytes()));
        writer.add("assets/fcLaunch/android.sha256", encoding.sha256Hash(classDir.child("android.jar").readBytes()));

        // put the game's resources
        // do not put signature files and package-relatived files
        // since we'll keep the first file when files collision happen.
        for (Fi file : gameApk.list()) {
            if (file.isDirectory()) {
                if (file.name().toLowerCase().equals("meta-inf")) {
                    for (Fi sub : file.list()) {
                        if (sub.isDirectory()) {
                            writer.add(file.name() + "/" + sub.name(), sub);
                        } else {
                            String fn = sub.name().toLowerCase();
                            if (fn.endsWith(".rsa") || fn.endsWith(".sf") || fn.equals("manifest.mf")) continue;
                            writer.add(file.name() + "/" + sub.name(), sub.readBytes());
                        }
                    }
                } else {
                    writer.add(file.name(), file);
                }
            } else {
                String fn = file.name().toLowerCase();
                if (fn.startsWith("classes") && fn.endsWith(".dex")) continue;
                if (fn.equals("resources.arsc")) continue;
                if (fn.equals("androidmanifest.xml")) continue;
                writer.add(file.name(), file.readBytes());
            }
        }

        writer.close();

        // sign apk
        apkSigner.init();
        Fi signedApk = apkSigner.sign(patchedApk);

        Vars.ui.showOkText(bundle.get("installer.exportPatchedApk.title"), bundle.get("installer.exportPatchedApk.hint"), () -> {
            BaseDialog dialog = new BaseDialog(bundle.get("installer.exportPatchedApk.title"));

            dialog.hidden(finalCampaign::safetyExit);
            dialog.addCloseButton();
            dialog.addCloseListener();

            dialog.cont.add(bundle.get("installer.exportPatchedApk.hint"));
            dialog.buttons.button(bundle.get("export"), Icon.export, () -> {
                Vars.platform.showFileChooser(false, bundle.get("installer.exportPatchedApk.title"), "apk", f -> {
                    signedApk.copyTo(f);
                    patchedApk.delete();
                    setting.put("installer.installedVersion", bothLauncherVersion.toVersionString());
                    dialog.hide();
                });
            });

            dialog.show();
        });
        
    }

    private static void installDesktop() throws Exception {
        Fi gameJar = new Fi(mindustry.Vars.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        if (!gameJar.absolutePath().toLowerCase().endsWith(".jar"))
            throw new RuntimeException("Could not locate mindustry jar file form class path.");
        
        Fi path = gameJar.parent();
        Fi configFi = path.child("fcConfig.properties");

        bothVersionControl.init(inInstalledGame());
        bothVersionControl.install(finalCampaign.thisMod.file.absolutePath(), version.toVersionString());

        String script = """
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

        Fi scriptFile = path.child("fcLaunch." + (OS.isWindows ? "bat" : "sh"));
        if (scriptFile.exists()) scriptFile.delete();
        scriptFile.writeString(script);

        config configSrc = new config();
        configSrc.gameJarName = gameJar.name();
        configSrc.dataDir = Core.settings.getDataDirectory().absolutePath();
        configSrc.isServer = Vars.headless;
        bothConfigUtil.write(configSrc, configFi.writer(false));

        Log.info("installer: done.");

        if (!Vars.headless) {
            Vars.ui.showOkText(bundle.get("info"), String.format(bundle.get("installer.finishHint"), scriptFile.absolutePath()), () -> {});
        } else {
            Log.info("[finalCampaign] " + bundle.get("info") + ": " + String.format(bundle.get("installer.finishHint"), scriptFile.absolutePath()));
        }
    }
}
