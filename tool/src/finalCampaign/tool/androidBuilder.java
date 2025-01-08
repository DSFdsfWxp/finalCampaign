package finalCampaign.tool;

import java.io.*;
import arc.util.*;
import finalCampaign.launch.*;
import finalCampaign.tool.apkUtil.*;
import finalCampaign.tool.io.*;
import finalCampaign.tool.mixinTransformer.*;
import finalCampaign.tool.patcher.*;

public class androidBuilder {

    // usage: androidBuilder <apk> <apkSigner> <d8> <androidSdkJar>
    public static void main(String[] args) throws Exception {
        fi rootDir = new fi(new File(""));
        fi apk = new fi(args[0]);
        String androidSdkJarPath = args[3];
        fi tmpDir = rootDir.child("build/android");
        cmdExecuter apkSignerExecuter = new cmdExecuter(args[1]);
        cmdExecuter d8Executer = new cmdExecuter(args[2]);

        if (!apk.exists()) {
            Log.err("Source apk is not existed.");
            System.exit(1);
        }

        tmpDir.mkdirs();

        fi apkJar = tmpDir.child("game.jar");
        dex2jar d2j = new dex2jar(apk);
        d2j.run(apkJar);

        fi mod = rootDir.child("android/build/lib/android.jar");
        fi patchedJar = tmpDir.child("patched.jar");
        jarWriter writer = new jarWriter(patchedJar, false);
        mixinTransformer transformer = new mixinTransformer(args);

        if (patchedJar.exists())
            patchedJar.delete();

        transformer.addSourceJar(apkJar);
        transformer.addSourceJar(mod);

        transformer.transform(writer);
        writer.close();

        fi dexJar = tmpDir.child("dex.jar");
        jar2dex j2d = new jar2dex(d8Executer, androidSdkJarPath, patchedJar);
        j2d.run(dexJar);

        fi unsignedApk = tmpDir.child("unsigned.apk");
        jarWriter apkWriter = new jarWriter(unsignedApk, true);

        if (unsignedApk.exists())
            unsignedApk.delete();
        
        fi assetsJar = rootDir.child("core/build/lib/assets.jar");
        fi apkZip = new zipFi(apk);
        fi dexZip = new zipFi(dexJar);

        dexZip.walk(dex -> {
            apkWriter.add(dex.absolutePath(), dex.readBytes());
        });

        allFileWalker walker = new allFileWalker(apkZip, f -> {
            if (!f.extension().equals("class") &&
                !f.extension().equals("dex"))
                    apkWriter.add(f.absolutePath(), f.readBytes());
        });
        walker.walk();

        apkWriter.add("/fcStandAloneMod.jar", assetsJar.readBytes());

        fi am = apkZip.child("AndroidManifest.xml");
        xmlPatcher xPatcher = new xmlPatcher(am.readBytes());
        xmlPatcher.xmlItem xManifest = xPatcher.manifest.xml.child.get(0);
        xmlPatcher.tagAttribute xPackageNameTag = xManifest.findAttribute("package", xmlPatcher.StringChunkType);
        String xPackageName = xPackageNameTag.readAsString();
        xPatcher.replaceString(xPackageName, xPackageName + ".fcMod");
        apkWriter.add("/AndroidManifest.xml", xPatcher.build());

        fi res = apkZip.child("resources.arsc");
        arscPatcher aPatcher = new arscPatcher(res.readBytes());
        aPatcher.replacePackageName(xPackageName, xPackageName + ".fcMod");
        aPatcher.replaceString("Mindustry BE", "Mindustry");
        aPatcher.replaceString("Mindustry", "Fc Mindustry");
        apkWriter.add("/resources.arsc", aPatcher.build());

        apkWriter.close();

        fi cert = rootDir.child("tool/res/cert.pem");
        fi key = rootDir.child("tool/res/key.pem");
        fi out = rootDir.child("build/lib/fcMindustry.apk");
        apkSigner signer = new apkSigner(apkSignerExecuter, unsignedApk);
        signer.setKey(cert, key);
        signer.sign(out);
    }
}
