package finalCampaign.net;

import java.net.*;
import arc.*;
import finalCampaign.dialog.*;

public class uriProcessor {
    public static boolean process(URI uri) {
        if (uri.getHost().equals("about")) {
            aboutDialog about = new aboutDialog();
            about.show();
            return true;
        } else if (uri.getHost().equals("repo")) {
            Core.app.openURI("https://github.com/DSFdsfWxp/finalCampaign");
            return true;
        }
        return false;
    }
}
