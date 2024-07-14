package finalCampaign.patch.impl;

import arc.struct.Seq;
import arc.util.*;
import org.spongepowered.asm.mixin.*;
import mindustry.core.*;
import mindustry.*;
import mindustry.ctype.*;

@Mixin(ContentLoader.class)
public abstract class fcContentLoader {
    public void logContent() {
        Log.info("test!!!!");
    }

    @Shadow(remap = false)
    private Seq<Content>[] contentMap;

    public void test() {
        Log.info("yeah!!!");
        Log.info(this.contentMap);
        Vars.ui.showOkText("test", "ok", () -> {});
    }
}
