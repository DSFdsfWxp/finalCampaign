package finalCampaign.ui.layout;

import arc.scene.*;
import arc.scene.ui.layout.*;

public abstract class dragLayout extends WidgetGroup {
    Element dragging;
    Runnable indexUpdater;

    public void indexUpdate(Runnable updater) {
        indexUpdater = updater;
    }

    public void drag(Element e) {
        dragging = e;
    }

    public boolean dragging() {
        return dragging != null;
    }

    public void drag() {
        finishLayout();
    }

    abstract void finishLayout();
    public abstract void layout();
}
