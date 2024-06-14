package finalCampaign;

import arc.*;
import arc.util.*;
import arc.files.*;
import arc.util.Log.*;
import mindustry.*;
import mindustry.core.ContentLoader;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import finalCampaign.patch.*;
import finalCampaign.patch.patchClass.*;

import static mindustry.Vars.*;

@SuppressWarnings({"rawtypes", "unchecked"})
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

            try {
                pool.init();
                Log.info("inited pool.");

                pool.patchAndCache(fcContentLoader.class);
                Log.info("patched target.");

                Class patchedClass = pool.resolve(fcContentLoader.class);
                Object patchedObject = patchedClass.getDeclaredConstructor(Object.class).newInstance((Object)content.getContentMap());
                Log.info("resolved and instantiated target.");

                Class proxyClass = modifyRuntime.resolveProxyClass(fcContentLoader.class);
                Object proxyObject = proxyClass.getDeclaredConstructor().newInstance();
                Log.info("resolved and instantiated proxy.");

                modifyRuntime.setProxyTarget(fcContentLoader.class, proxyObject, patchedObject);
                Log.info("set proxy target.");

                content = (ContentLoader) proxyObject;
                Log.info("replaced contentLoader.");
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
