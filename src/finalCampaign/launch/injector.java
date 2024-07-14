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
                gameJar = shareMixinService.thisJar;
            } else {
                throw new RuntimeException("Could not resolve class path.");
            }
        }

        if (!gameJar.absolutePath().toLowerCase().endsWith(".jar"))
            throw new RuntimeException("Could not locate mindustry jar file form class path.");
            
        Fi configFi = gameJar.parent().child("fcConfig.bin");
        if (configFi.exists()) {
            if (!bothLauncherVersion.toDesktopVersionString().equals(bothConfigUtil.read(configFi).version)) return false;
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

        if (!gameApkNotModified) {
            for (Fi jar : jarLst) {
                ObjectMap<Fi, byte[]> filesNeedToWrite = new ObjectMap<>();
                Seq<Fi> filedNeedToBeDeleted = new Seq<>();
    
                allFileWalker walker = new allFileWalker(jar, f -> {
                    if (!f.extension().equals("class")) return;
                    Log.info(f.absolutePath());
                    bothClassPatcher cPatcher = new bothClassPatcher(f.readBytes());
                    cPatcher.replaceString("$_CC", "$-CC", true, false, 7, 9, 10, 11, 12);
                    Fi nf = f.parent().child(f.name().replace("$_CC", "$-CC"));
                    if (!nf.name().equals(f.name()) || cPatcher.modified()) {
                        filedNeedToBeDeleted.add(f);
                        filesNeedToWrite.put(nf, cPatcher.build());
                    }
                });
                walker.walk();
    
                for (Fi f : filedNeedToBeDeleted) f.delete();
                for (Fi f : filesNeedToWrite.keys()) f.writeBytes(filesNeedToWrite.get(f));
            }
        }

        Fi forcePreLoadLstFi = tmpDir.child("forcePreload.list");
        if (!gameApkNotModified || !forcePreLoadLstFi.exists()) {
            Seq<String> forcePreLoadLst = new Seq<>();
            for (Fi jar : jarLst) {
                for (Fi sub : jar.list()) {
                    if (sub.isDirectory()) {
                        if (!sub.name().equals("mindustry")) {
                            String rootPath = sub.absolutePath();
                            allFileWalker walker = new allFileWalker(sub, f -> {
                                if (!f.extension().equals("class")) return;
                                // mindustry won't appear in its dependencies, only r8 will do that.
                                if (f.readString().indexOf("mindustry") > -1) {
                                    String name = f.nameWithoutExtension();
                                    name = name.substring(rootPath.length());
                                    if (name.startsWith("/")) name = name.substring(1);
                                    forcePreLoadLst.add(name.replace('/', '.'));
                                }
                            });
                            walker.walk();
                        }
                    }
                }
            }
            if (forcePreLoadLstFi.exists()) forcePreLoadLstFi.delete();
            forcePreLoadLstFi.writeString(String.join("\n", forcePreLoadLst));
        }

        Fi gameJar = tmpDir.child("game.jar");
        jarWriter writer = null;

        if (!gameJar.exists() || !gameApkNotModified) {
            if (gameJar.exists()) gameJar.delete();
            writer = new jarWriter(gameJar, false);

            for (Fi jar : jarLst) {
                Fi mindustryDir = jar.child("mindustry");
                Fi arcDir = jar.child("arc");
                if (mindustryDir.exists()) writer.add(mindustryDir.name(), mindustryDir);
                if (arcDir.exists()) writer.add(arcDir.name(), arcDir);
            }
    
            writer.close();
        }

        if (!gameApkNotModified) hash.writeString(gameApkHash);

        Fi classFi = tmpDir.child("classes2.dex");
        if (!classFi.exists() || !gameApkNotModified) {
            if (classFi.exists()) classFi.delete();
            jar2dex jar2dex = new jar2dex();

            for (Fi jar : jarLst) {
                for (Fi file : jar.list()) {
                    if (file.isDirectory() && !file.name().equals("mindustry")) {
                        jar2dex.add(file.name(), file);
                    }
                }
            }
    
            jar2dex.writeTo(classFi.write());
        }

        Fi manifest = gameApk.child("AndroidManifest.xml");
        xmlPatcher xPatcher = new xmlPatcher(manifest.readBytes());
        xPatcher.replaceString(packageName, packageName + ".finalCampaignMod");
        xPatcher.replaceString(className, "finalCampaign.launch.androidLauncher");
        byte[] patchedManifest = xPatcher.build();

        Fi resources = gameApk.child("resources.arsc");
        arscPatcher aPatcher = new arscPatcher(resources.readBytes());
        aPatcher.replaceString(applicationName, "Fc " + applicationName);
        aPatcher.replacePackageName(packageName, packageName + ".finalCampaignMod");
        byte[] patchedResource = aPatcher.build();

        Fi patchedApk = tmpDir.child("patched.apk");
        if (patchedApk.exists()) patchedApk.delete();
        writer = new jarWriter(patchedApk, true);

        Fi preMainJar = finalCampaign.thisModFi.child("class").child("preMain.android.jar");
        Fi preMainJarAsZip = tmpDir.child("preMain.jar");
        if (preMainJarAsZip.exists()) preMainJarAsZip.delete();
        preMainJar.copyTo(preMainJarAsZip);
        preMainJarAsZip = new ZipFi(preMainJarAsZip);
        writer.add("", preMainJarAsZip);

        writer.add("AndroidManifest.xml", patchedManifest);
        writer.add("resources.arsc", patchedResource, ZipOutputStream.STORED);
        writer.add("assets/fcLaunch/game.jar", gameJar.readBytes());
        writer.add("assets/fcLaunch/mod.jar", finalCampaign.thisMod.file.readBytes());
        writer.add("assets/fcLaunch/forcePreload.list", forcePreLoadLstFi.readBytes());
        writer.add("classes2.dex", classFi.readBytes());

        Fi javaJar = finalCampaign.thisModFi.child("class").child("java.jar");

        writer.add("assets/fcLaunch/mod.version", String.format("%d.%d.%s", version.major, version.minor, version.type).getBytes(StandardCharsets.UTF_8));
        writer.add("assets/fcLaunch/game.sha256", encoding.sha256Hash(gameJar.readBytes()));
        writer.add("assets/fcLaunch/java.sha256", encoding.sha256Hash(javaJar.readBytes()));

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
                if (!file.name().equals("mindustry")) writer.add(file.name(), file);
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
        bothConfigUtil.write(configSrc, configFi);

        Log.info("injector:  -> " + patchedFile.absolutePath());
        Log.info("injector: done.");

        SDL.SDL_ShowSimpleMessageBox(SDL.SDL_MESSAGEBOX_INFORMATION, bundle.get("info"), String.format(bundle.get("injector.finishHint"), scriptFile.absolutePath()));
        finalCampaign.safetyExit();
    }
}
