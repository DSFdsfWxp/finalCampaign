package finalCampaign.launch;

import java.net.*;
import java.nio.charset.*;
import java.util.zip.*;
import arc.*;
import arc.backend.sdl.jni.*;
import arc.files.*;
import arc.struct.*;
import arc.util.*;
import finalCampaign.*;
import finalCampaign.launch.bothConfigUtil.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.ui.dialogs.*;

public class injector {
    public static void inject() throws Exception {
        if (Vars.ios) throw new RuntimeException("Wait...Java mod on ios? How can this happened?");

        if (Vars.mobile) {
            injectAndroid();
        } else {
            injectDesktop();
        }
    }

    public static boolean inInjectedGame() {
        try {
            Class.forName("finalCampaign.launch.shareLogger", true, Thread.currentThread().getContextClassLoader());
        } catch(Exception e) {
            return false;
        }

        return true;
    }

    @SuppressWarnings("resource")
    public static boolean injected() {
        if (Vars.mobile) {
            if (inInjectedGame()) return true;
            //return ((String) setting.get("injector.injectedVersion", "")).equals(bothLauncherVersion.toAndoridVersionString());
            return false;
        }

        URL url = mindustry.Vars.class.getProtectionDomain().getCodeSource().getLocation();
        Fi gameJar;

        if (url != null) {
            gameJar = new Fi(url.getFile());
        } else {
            if (inInjectedGame()) {
                gameJar = new Fi(shareMixinService.getClassPath());
            } else {
                throw new RuntimeException("Could not resolve class path.");
            }
        }

        if (!gameJar.absolutePath().toLowerCase().endsWith(".jar"))
            throw new RuntimeException("Could not locate mindustry jar file form class path.");
            
        Fi configFi = gameJar.parent().child("fcConfig.bin");
        if (configFi.exists()) {
            if (!bothLauncherVersion.toDesktopVersionString().equals(bothConfigUtil.read(configFi.read()).version)) return false;
            return true;
        } else {
            return false;
        }
    }

    private static void injectAndroid() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Object applicationInfo = Core.app.getClass().getMethod("getApplicationInfo").invoke(Core.app);
        Object packageManager = Core.app.getClass().getMethod("getPackageManager").invoke(Core.app);

        String packageName = (String) Core.app.getClass().getMethod("getPackageName").invoke(Core.app);
        String applicationName = (String) applicationInfo.getClass().getMethod("loadLabel", classLoader.loadClass("android.content.pm.PackageManager")).invoke(applicationInfo, packageManager);
        String className = Core.app.getClass().getName();
        String apkPath = (String) applicationInfo.getClass().getDeclaredField("sourceDir").get(applicationInfo);

        Fi tmpDir = finalCampaign.dataDir.child("injector");
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

        Vars.ui.showOkText(bundle.get("injector.exportPatchedApk.title"), bundle.get("injector.exportPatchedApk.hint"), () -> {
            BaseDialog dialog = new BaseDialog(bundle.get("injector.exportPatchedApk.title"));

            dialog.hidden(finalCampaign::safetyExit);
            dialog.addCloseButton();
            dialog.addCloseListener();

            dialog.cont.add(bundle.get("injector.exportPatchedApk.hint"));
            dialog.buttons.button(bundle.get("export"), Icon.export, () -> {
                Vars.platform.showFileChooser(false, bundle.get("injector.exportPatchedApk.title"), "apk", f -> {
                    signedApk.copyTo(f);
                    patchedApk.delete();
                    setting.put("injector.injectedVersion", bothLauncherVersion.toAndoridVersionString());
                    dialog.hide();
                });
            });

            dialog.show();
        });
        
    }

    private static void injectDesktop() throws Exception {
        Fi gameJar = new Fi(mindustry.Vars.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        if (!gameJar.absolutePath().toLowerCase().endsWith(".jar"))
            throw new RuntimeException("Could not locate mindustry jar file form class path.");
        
        ZipFi gameJarAsZip = new ZipFi(gameJar);
        ZipFi modJar = finalCampaign.thisModFi;
        Fi path = gameJar.parent();

        Fi tmp = path.child("fcTmp");
        if (tmp.exists()) tmp.delete();
        tmp.write(modJar.child("class").child("preMain.desktop.jar").read(), false);
        ZipFi preMainJar = new ZipFi(tmp);

        Fi patchedFile = path.child("fcMindustry.patched.jar");
        Fi configFi = path.child("fcConfig.bin");

        if (patchedFile.exists()) patchedFile.delete();
        jarWriter writer = new jarWriter(patchedFile, false);

        writer.add("", preMainJar);

        for (Fi file : gameJarAsZip.list()) {
            if (file.isDirectory()) {
                if (!file.name().equals("mindustry") &&
                    !file.name().equals("arc") &&
                    !file.name().equals("net") &&
                    !file.name().equals("rhino")) writer.add(file.name(), file);
            } else {
                writer.add(file.name(), file.readBytes());
            }
        }
        
        writer.close();
        preMainJar.delete();
        tmp.delete();

        String script = """
@echo off
title finalCampaign Mod - Mindustry
java -jar ./fcMindustry.patched.jar
if %ERRORLEVEL% EQU 0 goto f
echo.
echo The game crash or your java is not installed correctly.
:f
pause
        """;
        if (!OS.isWindows) 
            script = """
#!/bin/sh
java -jar ./fcMindustry.patched.jar
            """;

        Fi scriptFile = path.child("fcLaunch." + (OS.isWindows ? "bat" : "sh"));
        if (scriptFile.exists()) scriptFile.delete();
        scriptFile.writeString(script);

        config configSrc = new config();
        configSrc.appName = Core.settings.getAppName();
        configSrc.version = bothLauncherVersion.toDesktopVersionString();
        configSrc.modName = finalCampaign.thisMod.file.name();
        configSrc.gameJarName = gameJar.name();
        configSrc.dataDir = Core.settings.getDataDirectory().absolutePath();
        bothConfigUtil.write(configSrc, configFi.write());

        Log.info("injector:  -> " + patchedFile.absolutePath());
        Log.info("injector: done.");

        SDL.SDL_ShowSimpleMessageBox(SDL.SDL_MESSAGEBOX_INFORMATION, bundle.get("info"), String.format(bundle.get("injector.finishHint"), scriptFile.absolutePath()));
        finalCampaign.safetyExit();
    }
}
