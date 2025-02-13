package finalCampaign.feature.hudUI;

import arc.*;
import arc.input.*;
import arc.scene.*;
import arc.scene.ui.layout.*;
import finalCampaign.*;
import finalCampaign.event.*;
import finalCampaign.ui.*;

public class hudWindowLayer {

    private WidgetGroup layer;
    private window focusedWindow;

    public void setup(Group parent) {
        layer = new WidgetGroup();
        layer.setFillParent(true);
        layer.name = "fcHudUIWindowLayer";

        parent.addChild(layer);

        Events.on(fcInputHandleUpdateEvent.class, e -> {
            if (e.atHead)
                return;

            if (Core.input.keyDown(KeyCode.mouseLeft)) {
                var element = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);

                for (var c : layer.getChildren()) {
                    if (c instanceof window w) {
                        if (element == w || element.isDescendantOf(w)) {
                            if (focusedWindow != w) {
                                bringWindowToTop(w);
                                focusWindow(w);
                            }
                            return;
                        }
                    }
                }
            }

            focusWindow(null);
        });
    }

    public void bringWindowToTop(window window) {
        var children = layer.getChildren();

        if (!children.contains(window))
            return;

        children.remove(window);
        children.add(window);
    }

    public void focusWindow(window window) {
        if (window == focusedWindow)
            return;

        if (focusedWindow != null)
            focusedWindow.blur();
        if (window != null)
            window.focus();

        focusedWindow = window;
    }

    public void showWindow(window window) {
        showWindow(window, false);
    }

    public void showWindow(window window, boolean rememberPosition) {
        if (layer.getChildren().contains(window))
            return;

        layer.addChild(window);

        if (rememberPosition) {
            window.closed(() -> {
                setting.put("hudUI.windowLayer." + window.name + ".position.x", window.x);
                setting.put("hudUI.windowLayer." + window.name + ".position.y", window.y);
            });

            window.x = setting.getAndCast("hudUI.windowLayer." + window.name + ".position.x", window.x);
            window.y = setting.getAndCast("hudUI.windowLayer." + window.name + ".position.y", window.y);
        }

        window.closed(() -> removeWindow(window));
        window.show();
        focusWindow(window);
    }

    public void removeWindow(window window) {
        var children = layer.getChildren();

        if (!children.contains(window))
            return;

        children.remove(window);
        if (window == focusedWindow)
            focusWindow(children.size > 0 ? (window) children.peek() : null);
    }

    public void resetWindowPosition(window window) {
        if (window.isShown())
            window.close();

        setting.remove("hudUI.windowLayer." + window.name + ".position.x");
        setting.remove("hudUI.windowLayer." + window.name + ".position.y");
    }
}
