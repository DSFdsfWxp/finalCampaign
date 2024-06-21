package finalCampaign;

import arc.*;
import arc.util.*;
import arc.util.io.PropertiesUtils;
import arc.files.*;
import arc.struct.*;

public class bundle {

    public static Fi bundleCacheDir;

    public static void init() {
        bundleCacheDir = finalCampaign.dataDir.child("bundle");
        if (!bundleCacheDir.exists()) bundleCacheDir.mkdirs();
    }

    public static void clearCache() {
        if (bundleCacheDir.exists()) {
            bundleCacheDir.deleteDirectory();
            bundleCacheDir.mkdirs();
        }
    }

    public static void load() {
        String str = Core.bundle.getLocale().toString();
        String fileName = str.isEmpty() ? "default" : str;

        Fi bundleFile = bundleCacheDir.child(fileName + ".properties");

        if (!bundleFile.exists()) {
            Fi rawBundleFile = finalCampaign.thisModFi.child("fcBundle").child(bundleFile.name());
            String[] bundleContent = rawBundleFile.readString().split("\n");
            Seq<String> processedBundleContent = new Seq<>();

            asyncTask.subTask(new Thread(() -> {
                for (String txt : bundleContent) {
                    String out = txt.trim();
                    if (out.startsWith("[raw].")) {
                        out = out.substring(6);
                    } else {
                        if (!out.isEmpty()) out = "finalCampaign." + out;
                    }
                    processedBundleContent.add(out);
                }
            }));

            asyncTask.subTask(() -> {
                bundleFile.writeString(String.join("\n", processedBundleContent));
            });
        }

        asyncTask.subTask(() -> {
            I18NBundle bundle = Core.bundle;
            while (bundle != null) {
                try {
                    PropertiesUtils.load(bundle.getProperties(), bundleFile.reader());
                } catch(Exception e) {
                    Log.err("bundle: failed to load bundle: " + fileName, e);
                }
    
                bundle = bundle.getParent();
            }
        });
    }

    public static String get(String name) {
        return Core.bundle.get("finalCampaign." + name);
    }

    public static String format(String name, Object ...args) {
        return Core.bundle.format("finalCampaign." + name, args);
    }
}
