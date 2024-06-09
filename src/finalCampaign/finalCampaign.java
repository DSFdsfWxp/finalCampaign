package finalCampaign;

import arc.*;
import arc.util.*;
import arc.util.Log.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import finalCampaign.patch.*;

import static mindustry.Vars.*;

public class finalCampaign extends Mod{

    public finalCampaign(){
        Log.info(" # finalCampaign [prototypePhase]");
        Log.info(" # v0.0.1");

        Log.level = LogLevel.debug;

        //listen for game load event
        Events.on(ClientLoadEvent.class, e -> {
            content = new fcContentLoader(content.getContentMap());
        });
    }

    @Override
    public void loadContent(){
        //Log.info("Loading some example content.");
    }

}
