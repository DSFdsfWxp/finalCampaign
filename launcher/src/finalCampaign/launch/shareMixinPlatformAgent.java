package finalCampaign.launch;

import java.util.*;
import java.lang.reflect.*;
import org.spongepowered.asm.launch.platform.*;
import org.spongepowered.asm.launch.platform.container.*;
import org.spongepowered.asm.util.*;

public class shareMixinPlatformAgent extends MixinPlatformAgentDefault implements IMixinPlatformServiceAgent {
    public void init() {}

    public final String getSideName() {
        return Constants.SIDE_CLIENT;
    }

    @SuppressWarnings("unchecked")
    public Collection<IContainerHandle> getMixinContainers() {
        Map<IContainerHandle, MixinContainer> cMap;

        try {
            Field map = manager.getClass().getDeclaredField("containers");
            map.setAccessible(true);
            cMap = (Map<IContainerHandle, MixinContainer>) map.get(manager);
        } catch(Exception e) {
            return Collections.emptyList();
        }
        return cMap.keySet();
    }
}
