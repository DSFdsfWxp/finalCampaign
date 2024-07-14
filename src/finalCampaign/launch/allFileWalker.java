package finalCampaign.launch;

import arc.files.*;
import arc.func.*;

public class allFileWalker {
    private Fi root;
    private Cons<Fi> action;

    public allFileWalker(Fi root, Cons<Fi> action) {
        this.action = action;
        this.root = root;
    }

    private void walkDir(Fi dir) {
        for (Fi sub : dir.list()) {
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
