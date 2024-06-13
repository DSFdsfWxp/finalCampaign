package finalCampaign;

import arc.*;
import arc.util.*;
import arc.util.Log.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import finalCampaign.patch.*;

import static mindustry.Vars.*;

public class finalCampaign extends Mod{

    public static LoadedMod thisMod;

    public finalCampaign(){
        Log.info(" # finalCampaign [prototypePhase]");
        Log.info(" # v0.0.1");

        Log.level = LogLevel.debug;

        thisMod = mods.getMod(finalCampaign.class);

        //listen for game load event
        Events.on(ClientLoadEvent.class, event -> {
            try {
                pool.init();
            } catch(Exception e) {
                Vars.ui.showException(e);
            }
        });
    }

    @Override
    public void loadContent(){
        //Log.info("Loading some example content.");
    }

}
