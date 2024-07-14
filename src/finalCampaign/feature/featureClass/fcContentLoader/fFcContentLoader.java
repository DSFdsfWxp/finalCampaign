package finalCampaign.feature.featureClass.fcContentLoader;

import mindustry.Vars;

public class fFcContentLoader {

    public static void init() throws Exception {

    }

    public static void load() throws Exception {
        Vars.content = new fcContentLoader(Vars.content);
    }
}
