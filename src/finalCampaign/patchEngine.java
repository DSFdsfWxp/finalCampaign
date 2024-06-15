package finalCampaign;

import arc.*;
import arc.files.*;
import arc.util.Log;
import finalCampaign.patch.*;
import finalCampaign.patch.patchClass.*;
import mindustry.Vars;
import mindustry.core.*;
import mindustry.gen.*;
import mindustry.ui.dialogs.*;

public class patchEngine {
    public static boolean init() {
        BaseDialog dialog = null;
        Fi classDir = finalCampaign.dataDir.child("class");
        Fi mindustryClassFile = classDir.child("mindustry.jar");

        if (!classDir.exists()) classDir.mkdirs();

        if (Vars.android) {
            if (!mindustryClassFile.exists()) {

                // we only offer prebuild jar file to this version
                if (Version.build != 146 ||
                    Version.number != 7 ||
                    Version.revision != 0 ||
                    !Version.modifier.equals("release") ||
                    !Version.type.equals("official")) dialog = new BaseDialog("错误");
            }
        }

        if (dialog == null) {
            try {
                cache.init();
                pool.init();
            } catch(Exception e) {
                Log.err(e);
                Vars.ui.showException(e);

                return false;
            }

            return true;
        }

        dialog.addCloseListener();
        dialog.buttons.defaults().size(210f, 64f);

        // well, we'll support multi-languages, but not now.
        dialog.buttons.button("退出", Icon.exit, () -> {
            Core.app.exit();
        });
        dialog.hidden(() -> {
            Core.app.exit();
        });

        dialog.buttons.button("导入jar", Icon.download, () -> {
            Vars.platform.showFileChooser(true, "选择jar", "jar", (Fi file) -> {
                file.copyTo(mindustryClassFile);
                Vars.ui.showOkText("提示", "导入成功, 重启游戏生效. ", () -> {
                    Core.app.exit();
                });
            });
        });

        dialog.add("[red]finalCampaign 未能正常加载").left();
        dialog.add("原因: 不支持的游戏版本").left().padTop(10f);
        dialog.add("详情:").left();
        dialog.add("[lightgray]由于Android平台特性, 我们不能直接读取游戏本体的字节码, 因此我们借助了预置的").left();
        dialog.add("[lightgray]包含游戏字节码的jar包来保障本模组能在大部分Android平台上正常运行. 但是你的游").left();
        dialog.add("[lightgray]戏版本与预置jar包不匹配 (预置: 146 v7.0 official release)").left();
        dialog.add("解决方案:").left();
        dialog.add("[lightgray]提取游戏安装包 -> 当成压缩包解压 -> ").left();
        dialog.add("[lightgray]找到dex文件(如 classes.dex, classes2.dex, classes3.dex ...) ->").left();
        dialog.add("[lightgray]将这些dex文件转换为一个jar文件 -> 导入jar文件").left();

        dialog.show();
        return false;
    }

    public static void patch() {
        try {

            modify.patch(fcContentLoader.class);
            Log.info("patched target.");

        } catch(Exception e) {
            Vars.ui.showException(e);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void load() {
        try {

            Class<?> patchedClass = pool.resolveModifiedTargetClass(fcContentLoader.class, ContentLoader.class);
            Log.info("resolved target.");

            Object patchedObject = patchedClass.getDeclaredConstructor(Object.class).newInstance((Object) Vars.content.getContentMap());
            Log.info("instantiated target.");

            Class proxyClass = pool.resolveAllProxiedClass(ContentLoader.class);
            Object proxyObject = proxyClass.getDeclaredConstructor().newInstance();
            Log.info("resolved and instantiated proxy.");

            proxyRuntime.setProxyTarget(proxyObject, patchedObject);
            Log.info("set proxy target.");

            Vars.content = (ContentLoader) proxyObject;
            Log.info("replaced contentLoader.");

        } catch(Exception e) {
            Vars.ui.showException(e);
        }
    }
}
