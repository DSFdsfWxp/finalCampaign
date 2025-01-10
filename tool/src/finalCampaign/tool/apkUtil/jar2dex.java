package finalCampaign.tool.apkUtil;

import arc.struct.*;
import finalCampaign.launch.*;
import finalCampaign.tool.*;
import finalCampaign.tool.io.*;

public class jar2dex {
    private fi jar;
    private Seq<String> classpaths;
    private cmdExecuter executer;

    public jar2dex(cmdExecuter executer, String[] cps, fi jar) {
        this.jar = jar;
        this.executer = executer;
        classpaths = new Seq<>();

        for (String cp : cps) {
            classpaths.addAll("--classpath", cp);
        }
    }

    public void run(fi out) throws Exception {
        if (out.exists())
            out.delete();

        Seq<String> mainDexLst = new Seq<>();
        zipFi jarZip = new zipFi(jar);

        allFileWalker walker = new allFileWalker(jarZip, f -> {
            if (f.extension().equals("class")) {
                String path = f.path();
                if (path.startsWith("arc/") ||
                    path.startsWith("mindustry/") ||
                    path.startsWith("com/android/") ||
                    path.startsWith("androidx/"))
                        mainDexLst.add(path);
            }
        });
        
        walker.walk();
        jarZip.delete(); // close zip

        fi mainDexLstFi = out.parent().child("mainDexLst.txt");
        mainDexLstFi.writeString(String.join("\n", mainDexLst));

        Seq<String> args = new Seq<>();

        args.add(classpaths);
        args.add(new String[] {
            "--min-api",
            "14",
            "--main-dex-list",
            mainDexLstFi.absolutePath(),
            "--output",
            out.absolutePath(),
            jar.absolutePath()
        });

        if (executer.exec(args.toArray(String.class)) != 0)
            throw new RuntimeException("Failed to convert jar to dex");
    }
}
