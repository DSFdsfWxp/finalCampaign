package finalCampaign.tool;

import java.io.*;
import java.util.zip.*;
import arc.util.*;
import arc.util.Log.LogLevel;
import finalCampaign.launch.*;
import finalCampaign.tool.apkUtil.*;
import finalCampaign.tool.io.*;
import finalCampaign.tool.mixinTransformer.*;
import finalCampaign.tool.patcher.*;

public class androidBuilder {

    // usage: androidBuilder <apk> <jar> <apkSigner> <d8> <androidSdkJar>
    public static void main(String[] args) throws Exception {
        fi rootDir = new fi(new File(""));
        fi apk = new fi(args[0]);
        fi jar = new fi(args[1]);
        fi tmpDir = rootDir.child("build/android");
        String androidSdkJarPath = args[4];
        cmdExecuter apkSignerExecuter = new cmdExecuter(args[2]);
        cmdExecuter d8Executer = new cmdExecuter(args[3]);

        setupLogger();

        if (!apk.exists()) {
            Log.err("Source apk is not existed.");
            System.exit(1);
        }

        tmpDir.mkdirs();

        Log.info("[androidBuilder] dex to jar...");

        fi apkJar = tmpDir.child("apk.jar");
        dex2jar d2j = new dex2jar(apk);
        d2j.run(apkJar);

        Log.info("[androidBuilder] merging...");

        fi apkJarZip = new zipFi(apkJar);
        fi jarZip = new zipFi(jar);
        fi gameJar = tmpDir.child("game.jar");
        jarWriter mergeWriter = new jarWriter(gameJar, false);

        mergeWriter.add("androidx", apkJarZip.child("androidx"));
        mergeWriter.add("arc/backend", apkJarZip.child("arc").child("backend"));
        mergeWriter.add("mindustry/android", apkJarZip.child("mindustry").child("android"));

        mergeWriter.exclude("mindustry/server");
        mergeWriter.exclude("arc/backend/headless");

        mergeWriter.add("arc", jarZip.child("arc"));
        mergeWriter.add("mindustry", jarZip.child("mindustry"));
        mergeWriter.add("net", jarZip.child("net"));
        mergeWriter.add("rhino", jarZip.child("rhino"));

        mergeWriter.close();
        // close zip
        apkJarZip.delete();
        jarZip.delete();

        Log.info("[androidBuilder] patching...");

        fi mod = rootDir.child("android/build/libs/android.jar");
        fi patchedJar = tmpDir.child("patched.jar");
        fi mixinConfig = rootDir.child("core/assets/fcMixin/config.android.json");
        jarWriter writer = new jarWriter(patchedJar, false);
        mixinTransformer transformer = new mixinTransformer(args);

        transformer.addSourceJar(gameJar);
        transformer.addSourceJar(mod);

        transformer.setMixinConfig(mixinConfig);
        transformer.transform(writer);
        writer.close();

        Log.info("[androidBuilder] jar to dex...");

        fi dexJar = tmpDir.child("dex.jar");
        jar2dex j2d = new jar2dex(d8Executer, new String[] {androidSdkJarPath}, patchedJar);
        j2d.run(dexJar);

        fi unsignedApk = tmpDir.child("unsigned.apk");
        jarWriter apkWriter = new jarWriter(unsignedApk, true);

        Log.info("[androidBuilder] merging...");
        
        fi apkZip = new zipFi(apk);
        fi assetsJar = rootDir.child("core/build/libs/assets.jar");
        fi dexZip = new zipFi(dexJar);

        dexZip.walk(dex -> {
            apkWriter.add(dex.path(), dex.readBytes());
        });

        allFileWalker walker = new allFileWalker(apkZip, f -> {
            if (!f.extension().equals("class") &&
                !f.extension().equals("dex") && 
                !f.path().equals("AndroidManifest.xml") &&
                !f.path().equals("resources.arsc") &&
                !(f.path().toLowerCase().startsWith("meta-inf/") && !f.path().toLowerCase().startsWith("meta-inf/com/")))
                    apkWriter.add(f.path(), f.readBytes());
        });
        walker.walk();

        byte[] assetsJarData = assetsJar.readBytes();
        String assetsJarSha256 = encoding.bytesToHex(encoding.sha256Hash(assetsJarData));
        apkWriter.add("fcStandAloneMod.jar", assetsJarData);
        apkWriter.add("fcStandAloneMod.jar.sha256", assetsJarSha256.getBytes());

        Log.info("[androidBuilder] patching app info...");

        fi am = apkZip.child("AndroidManifest.xml");
        xmlPatcher xPatcher = new xmlPatcher(am.readBytes());
        xmlPatcher.xmlItem xManifest = xPatcher.manifest.xml.child.get(0);
        xmlPatcher.tagAttribute xPackageNameTag = xManifest.findAttribute("package", xmlPatcher.AttrStringType);
        String xPackageName = xPackageNameTag.readAsString();
        xPatcher.replaceString(xPackageName, xPackageName + ".fcMod");
        apkWriter.add("AndroidManifest.xml", xPatcher.build());

        fi res = apkZip.child("resources.arsc");
        arscPatcher aPatcher = new arscPatcher(res.readBytes());
        aPatcher.replacePackageName(xPackageName, xPackageName + ".fcMod");
        aPatcher.replaceString("Mindustry BE", "Mindustry");
        aPatcher.replaceString("Mindustry", "Mindustry FC");
        apkWriter.add("resources.arsc", aPatcher.build(), ZipOutputStream.STORED);

        apkWriter.close();
        // close zip
        apkZip.delete();
        dexZip.delete();

        Log.info("[androidBuilder] signing...");

        fi cert = rootDir.child("tool/res/cert.pem");
        fi key = rootDir.child("tool/res/key.pk8");
        fi out = rootDir.child("build/libs/MindustryFc.apk");
        apkSigner signer = new apkSigner(apkSignerExecuter, unsignedApk);
        signer.setKey(cert, key);
        signer.sign(out);

        Log.info("[androidBuilder] done.");
    }

    public static void setupLogger() {
        String[] stags = {"&lc&fb[D]", "&lb&fb[I]", "&ly&fb[W]", "&lr&fb[E]", ""};

        Log.level = LogLevel.debug;
        Log.logger = (level, text) -> {
            System.out.println(Log.format(stags[level.ordinal()] + "&fr " + text));
        };
    }
}
