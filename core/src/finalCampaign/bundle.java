package finalCampaign;

import java.util.*;
import arc.*;
import arc.util.*;
import arc.util.io.*;
import arc.files.*;
import arc.struct.*;

public class bundle {
    public static Fi bundleCacheDir;
    public static boolean loaded = false;
    public static String bundleVersion;

    public static void init() {
        bundleCacheDir = finalCampaign.dataDir.child("fcBundle");
        if (!bundleCacheDir.exists()) bundleCacheDir.mkdirs();
        if (checkUpdate()) clearCache();
    }

    public static boolean checkUpdate() {
        Fi bundleVersionFi = bundleCacheDir.child("version.properties");
        if (!bundleVersionFi.exists()) return true;
        ObjectMap<String, String> map = new ObjectMap<>();
        PropertiesUtils.load(map, bundleVersionFi.reader());
        return !map.get("version", "").equals(bundleVersion);
    }

    public static void clearCache() {
        if (bundleCacheDir.exists()) {
            bundleCacheDir.deleteDirectory();
            bundleCacheDir.mkdirs();
        }
    }

    public static String getLocaleString() {
        Locale locale = Locale.getDefault();
        String str = locale.getLanguage() + "_" + locale.getCountry();
        return str.equals("_") ? "en_US" : str;
    }

    public static void load() {
        if (Core.bundle == null) Core.bundle = I18NBundle.createEmptyBundle();
        String fileName = getLocaleString();

        Fi bundleFile = bundleCacheDir.child(fileName + ".properties");

        if (!bundleFile.exists()) {
            Fi rawBundleFile = finalCampaign.thisModZip.child("fcBundle").child(bundleFile.name());
            if (!rawBundleFile.exists()) rawBundleFile = rawBundleFile.parent().child("en_US.properties");
            String[] bundleContent = rawBundleFile.readString().split("\n");
            Seq<String> processedBundleContent = new Seq<>();

            for (String txt : bundleContent) {
                String out = txt.trim();
                if (out.startsWith("[raw].")) {
                    out = out.substring(6);
                } else {
                    if (!out.isEmpty()) out = "finalCampaign." + out;
                }
                processedBundleContent.add(out);
            }

            bundleFile.writeString(String.join("\n", processedBundleContent));
            Fi bundleVersionFile = bundleCacheDir.child("version.properties");
            ObjectMap<String, String> map = new ObjectMap<>();
            map.put("version", bundleVersion);
            try {
                PropertiesUtils.store(map, bundleVersionFile.writer(false), null);
            } catch(Exception e) {
                Log.err(e);
            }
        }

        I18NBundle bundle = Core.bundle;
        while (bundle != null) {
            try {
                PropertiesUtils.load(bundle.getProperties(), bundleFile.reader());
            } catch(Exception e) {
                Log.err("bundle: failed to load bundle: " + fileName, e);
            }

            bundle = bundle.getParent();
        }

        loaded = true;
    }

    public static String get(String name) {
        if (name.startsWith("@")) name = name.substring(1);
        return Core.bundle.get("finalCampaign." + name);
    }

    public static String get(String name, String fallback) {
        if (name.startsWith("@")) name = name.substring(1);
        return Core.bundle.get("finalCampaign." + name, fallback);
    }

    public static String format(String name, Object ...args) {
        if (name.startsWith("@")) name = name.substring(1);
        return Core.bundle.format("finalCampaign." + name, args);
    }

    public static class bundleNS {
        private final String namespace;
        
        public bundleNS(String namespace) {
            this.namespace = namespace;
        }

        public bundleNS appendNS(String ns) {
            return new bundleNS(namespace + "." + ns);
        }

        public String get(String name) {
            if (name.startsWith("@")) name = name.substring(1);
            return bundle.get(namespace + "." + name);
        }

        public String get(String name, String def) {
            if (name.startsWith("@")) name = name.substring(1);
            return bundle.get(namespace + "." + name, def);
        }

        public String format(String name, Object ...args) {
            if (name.startsWith("@")) name = name.substring(1);
            return bundle.format(namespace + "." + name, args);
        }
    }
}
