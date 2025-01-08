package finalCampaign.tool.mixinTransformer;

import arc.struct.*;
import arc.util.*;
import finalCampaign.launch.*;

public class transformerLauncher extends shareLauncher {
    public Seq<fi> src = new Seq<>();

    @Override
    protected void handleCrash(Throwable e, String msg) {
        Log.err(msg, e);
        System.exit(1);
    }

    @Override
    protected shareClassLoader createClassLoader() {
        return new transformerClassLoader();
    }

    @Override
    protected fi[] getJar() {
        return src.toArray(fi.class);
    }

    @Override
    protected void launch() throws Exception {}
    
}
