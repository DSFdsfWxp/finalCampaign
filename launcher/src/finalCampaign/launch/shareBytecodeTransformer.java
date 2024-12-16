package finalCampaign.launch;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.transformer.*;

public class shareBytecodeTransformer {
    private IMixinTransformer transformer;

    public shareBytecodeTransformer(IMixinTransformer transformer) {
        this.transformer = transformer;
    }

    public byte[] transform(String name, byte[] bytecode) {
        if (transformer == null) return null;
        if (name.startsWith("finalCampaign.")) return bytecode;

        try {
            if (bytecode == null) return transformer.generateClass(MixinEnvironment.getCurrentEnvironment(), name);
            return transformer.transformClassBytes(name, name, bytecode);
        } catch(Exception e) {
            throw new RuntimeException("failed to transform class: " + name, e);
        }
    }
}
