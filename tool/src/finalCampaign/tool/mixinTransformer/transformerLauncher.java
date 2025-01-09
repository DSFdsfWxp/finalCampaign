package finalCampaign.tool.mixinTransformer;

import java.io.*;
import arc.struct.*;
import arc.util.*;
import finalCampaign.launch.*;

public class transformerLauncher extends shareLauncher {
    public Seq<fi> src = new Seq<>();

    @Override
    public void init(String[] args) {
        files.ExternalStoragePath = OS.userHome + File.separator;
        files.LocalStoragePath = new File("").getAbsolutePath() + File.separator;
        
        files.instance = new files() {
            public fi internalFile(String path) {
                return new fi(path, fi.FileType.internal);
            }

            public fi dataDirectory() {
                return new fi(new File(""));
            }

            public fi rootDirectory() {
                return new fi(new File(""));
            }
        };

        super.init(args);
    }

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
