package finalCampaign;

import arc.*;
import arc.util.*;
import arc.files.*;
import arc.util.Log.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;

import static mindustry.Vars.*;

public class finalCampaign extends Mod{

    public static LoadedMod thisMod;
    public static Fi dataDir;

    public finalCampaign(){
        Log.info(" # finalCampaign [prototypePhase]");
        Log.info(" # v0.0.1");

        Log.level = LogLevel.debug;

        //listen for game load event
        Events.on(ClientLoadEvent.class, event -> {
            thisMod = mods.getMod(finalCampaign.class);

            dataDir = dataDirectory.child("finalCampaign");
            if (!dataDir.exists()) dataDir.mkdirs();

            if (!patchEngine.init()) return;

            patchEngine.patch();
            patchEngine.load();
        });
    }

    @Override
    public void loadContent(){
        //Log.info("Loading some example content.");
    }

}
