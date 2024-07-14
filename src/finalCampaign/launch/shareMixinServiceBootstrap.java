package finalCampaign.launch;

import arc.util.*;
import org.spongepowered.asm.service.*;

public class shareMixinServiceBootstrap implements IMixinServiceBootstrap {
    public String getName() {
        return "finalCampaign";
    }

    public String getServiceClassName() {
        return "finalCampaign.launch.shareMixinService";
    }

    public void bootstrap() {
        Log.info("[finalCampaign] mixin service bootstrap");
    }
}
