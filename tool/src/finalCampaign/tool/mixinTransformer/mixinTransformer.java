package finalCampaign.tool.mixinTransformer;

import finalCampaign.launch.*;
import finalCampaign.tool.io.*;

public class mixinTransformer {
    private transformerLauncher launcher;

    public mixinTransformer(String[] args) {
        launcher = new transformerLauncher();
        launcher.init(args);
    }
    
    public void addSourceJar(fi src) {
        launcher.src.add(src);
    }

    public void setMixinConfig(fi config) {
        shareMixinService.configFile = config;
    }

    public void transform(jarWriter writer) {
        launcher.startup();

        transformerClassLoader cl = (transformerClassLoader) shareMixinService.getClassLoader();
        cl.eachClassFile(f -> {
            for (transformerClassLoader.patchedClass pc : cl.patchClass(f.readBytes())) {
                writer.add(pc.name + ".class", pc.bytecode);
            }
        });
    }
}
