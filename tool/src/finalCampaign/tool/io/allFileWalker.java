package finalCampaign.tool.io;

import arc.func.*;
import finalCampaign.launch.*;

public class allFileWalker {
    private fi root;
    private Cons<fi> action;

    public allFileWalker(fi root, Cons<fi> action) {
        this.action = action;
        this.root = root;
    }

    private void walkDir(fi dir) {
        for (fi sub : dir.list()) {
            if (sub.isDirectory()) {
                walkDir(sub);
            } else {
                action.get(sub);
            }
        }
    }

    public void walk() {
        if (!root.isDirectory() || !root.exists()) throw new RuntimeException("Can not walk from a file or a non-existed directory: " + root.absolutePath());
        walkDir(root);
    }
}
