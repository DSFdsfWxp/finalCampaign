package finalCampaign.tool.apkUtil;

import finalCampaign.launch.*;
import finalCampaign.tool.*;

public class apkSigner {
    private fi apk;
    private cmdExecuter executer;
    private fi cert, key;

    public apkSigner(cmdExecuter executer, fi apk) {
        this.executer = executer;
        this.apk = apk;
    }

    public void setKey(fi cert, fi key) {
        this.cert = cert;
        this.key = key;
    }

    public void sign(fi out) throws Exception {
        if (out.exists())
            out.delete();

        String[] args = new String[] {
            "sign",
            "--cert", cert.absolutePath(),
            "--key", key.absolutePath(),
            "--v4-signing-enabled", "false",
            "--min-sdk-version", "14",
            "--out", out.absolutePath(),
            apk.absolutePath()
        };

        if (executer.exec(args) != 0)
            throw new RuntimeException("Failed to sign apk");
    }
}
