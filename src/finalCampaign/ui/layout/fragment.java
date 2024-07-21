package finalCampaign.ui.layout;

import arc.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.ui.layout.*;
import arc.util.*;

public class fragment extends Table {
    private boolean removed;

    public fragment() {
        this(null);
    }

    public fragment(Group parent) {
        if (parent == null) parent = Core.scene.root;
        setFillParent(false);
        color.a = 0f;
        setPosition(parent.x, parent.y);
        removed = true;
    }

    public boolean alive() {
        return removed;
    }

    public void added() {
        added(false, 0f, Interp.fade, null);
    }

    public void added(float fadeInDuration) {
        added(true, fadeInDuration, Interp.fade, null);
    }

    public void added(float fadeInDuration, Runnable callback) {
        added(true, fadeInDuration, Interp.fade, callback);
    }

    public void added(boolean fadeIn, float duration, @Nullable Interp interpolation, @Nullable Runnable callback) {
        Action callbackAction = new Action() {
            @Override
            public boolean act(float delta) {
                if (callback != null) callback.run();
                return true;
            }
        };
        if (fadeIn) {
            if (interpolation != null) {
                actions(Actions.fadeIn(duration, interpolation), callbackAction);
            } else {
                actions(Actions.fadeIn(duration), callbackAction);
            }
        } else {
            color.a = 1f;
        }

        removed = false;
    }

    @Override
    public boolean remove() {
        removed = true;
        return super.remove();
    }

    public void remove(float fadeOutDuration) {
        remove(true, fadeOutDuration, Interp.fade, null);
    }

    public void remove(float fadeOutDuration, Runnable callback) {
        remove(true, fadeOutDuration, Interp.fade, callback);
    }
    
    public boolean remove(boolean fadeOut, float duration, @Nullable Interp interpolation, @Nullable Runnable callback) {
        if (removed) return false;
        if (!fadeOut) return remove();

        Action callbackAction = new Action() {
            @Override
            public boolean act(float delta) {
                remove();
                if (callback != null) callback.run();
                return true;
            }
        };

        if (interpolation != null) {
            actions(Actions.fadeOut(duration, interpolation), callbackAction);
        } else {
            actions(Actions.fadeOut(duration), callbackAction);
        }

        removed = true;
        return true;
    }
}
