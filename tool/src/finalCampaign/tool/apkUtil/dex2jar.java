package finalCampaign.tool.apkUtil;

import com.googlecode.dex2jar.tools.*;
import finalCampaign.launch.*;

public class dex2jar {
    private fi apk;
    
    public dex2jar(fi apk) {
        this.apk = apk;
    }

    public void run(fi out) throws Exception {
        if (out.exists())
            out.delete();

        String[] args = new String[] {
            apk.absolutePath(),
            "-o",
            out.absolutePath()
        };
        Dex2jarCmd.main(args);
    }
}
