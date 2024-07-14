package finalCampaign.launch;

import java.io.*;

import arc.files.*;
import arc.struct.*;
import finalCampaign.com.android.dex.*;
import finalCampaign.com.android.dx.cf.direct.*;
import finalCampaign.com.android.dx.command.dexer.*;
import finalCampaign.com.android.dx.dex.*;
import finalCampaign.com.android.dx.dex.cf.*;
import finalCampaign.com.android.dx.dex.file.*;
import finalCampaign.com.android.dx.merge.*;

public class jar2dex {
    private CfOptions options;
    private DexOptions dexOptions;
    private Dex dex;
    
    public jar2dex() {
        dexOptions = new DexOptions();
        options = new CfOptions();

        dexOptions.minSdkVersion = 14;
    }

    public void add(String path, byte[] bytecode) {
        DxContext context = new DxContext();
        DexFile dexFile = new DexFile(dexOptions);
        
        DirectClassFile classFile = new DirectClassFile(bytecode, path, true);
        classFile.setAttributeFactory(StdAttributeFactory.THE_ONE);
        classFile.getMagic();
        dexFile.add(CfTranslator.translate(context, classFile, null, options, dexOptions, dexFile));

        Dex nDex = null;
        try {
            nDex = new Dex(dexFile.toDex(null, false));
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        if (dex == null) {
            dex = nDex;
        } else {
            try {
                dex = new DexMerger(new Dex[]{dex, nDex}, CollisionPolicy.KEEP_FIRST, context).merge();
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void writeTo(OutputStream stream) {
        try {
            dex.writeTo(stream);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void add(String path, Fi directory) {
        Seq<String> paths = new Seq<>(path.split("/"));
        for (Fi file : directory.list()) {
            paths.add(file.name());

            if (file.isDirectory()) {
                add(String.join("/", paths), file);
            } else {
                if (file.extension().equals("class")) add(String.join("/", paths), file.readBytes());
            }

            paths.pop();
        }
    }
}
