package finalCampaign.runtime.mixin;

import arc.util.*;
import java.io.*;
import arc.files.*;
import arc.func.*;
import arc.struct.*;
import finalCampaign.runtime.*;

public class mixinRuntime implements IRuntime {

    private Fi rootDir;
    private Fi modVersion, launcherVersion;

    public mixinRuntime(File rootDir) {
        if (rootDir == null) {
            try {
                rootDir = new File(mixinRuntime.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
            } catch (Exception e) {
                throw new RuntimeException("Should be ok.", e);
            }
        }

        this.rootDir = new Fi(rootDir);
        modVersion = this.rootDir.child("finalCampaign/mod/current");
        launcherVersion = this.rootDir.child("finalCampaign/launcher/current");

        clear();
    }

    @Override
    public String name() {
        return "Mixin";
    }

    @Override
    public Fi getRootPath() {
        if (OS.isAndroid)
            throw new RuntimeException("Mixin runtime is not available for Android");
        
        return rootDir;
    }

    @Override
    public String getVersion() {
        return launcherVersion.exists() ? launcherVersion.readString() : "0.0.0";
    }

    public void install(Fi mod) throws Exception {
        modVersion.parent().mkdirs();
        launcherVersion.parent().mkdirs();

    }

    private void clear() {
        Cons2<Fi, String> runClear = (f, ver) -> {
            for (Fi c : f.list()) {
                if (c.isDirectory() && !c.name().equals(ver))
                    c.deleteDirectory();
            }
        };

        if (modVersion.exists())
            runClear.get(modVersion.parent(), modVersion.readString());

        if (launcherVersion.exists())
            runClear.get(launcherVersion.parent(), launcherVersion.readString());
    }
    
}
