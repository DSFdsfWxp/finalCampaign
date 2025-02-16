package finalCampaign.feature.hudUI;

import arc.*;
import arc.input.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import finalCampaign.*;
import finalCampaign.event.*;
import finalCampaign.ui.*;
import mindustry.ui.*;

public class hudWindowLayer {

    private WidgetGroup layer;
    private ObjectMap<window, Table> flowIconMap;
    private window focusedWindow;

    public void setup(Group parent) {
        layer = new WidgetGroup();
        flowIconMap = new ObjectMap<>();

        layer.setFillParent(true);
        layer.name = "fcHudUIWindowLayer";

        parent.addChild(layer);

        Events.on(fcInputHandleUpdateEvent.class, e -> {
            if (e.beforeUpdate)
                return;

            if (Core.input.keyDown(KeyCode.mouseLeft)) {
                var element = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);

                if (element != null) {
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
            }

            focusWindow(null);
        });

        Events.on(fcInputKeyComboTapEvent.class, e -> {
            if (e.keycode != KeyCode.mouseLeft || e.count != 2)
                return;

            var element = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
            if (element == null)
                return;

            for (var window : flowIconMap.keys()) {
                if (element.isDescendantOf(window.iconImage)) {
                    var icon = flowIconMap.get(window);

                    if (icon.parent == null) {
                        fHudUI.bottomPopupLayer.addChild(icon);
                        icon.setPosition(window.x + window.iconImage.parent.x + window.iconImage.x, window.y + window.iconImage.parent.y + window.iconImage.y);
                    }

                    icon.color.a(0f);
                    icon.visible = true;
                    icon.touchable = Touchable.disabled;

                    icon.actions(
                            Actions.sequence(
                                    Actions.fadeOut(0.4f),
                                    Actions.touchable(Touchable.enabled)
                            )
                    );

                    Vec2 translateTarget = Tmp.v6.set(icon.x, icon.y).add(Scl.scl(48f) / 2f, Scl.scl(48f) / 2f)
                                    .sub(window.x, window.y).sub(window.originX, window.originY);

                    window.actions(
                        Actions.sequence(
                                Actions.touchable(Touchable.disabled),
                                Actions.parallel(
                                        Actions.translateBy(translateTarget.x, translateTarget.y, 0.4f),
                                        Actions.scaleTo(Scl.scl(48f) / window.getWidth(), Scl.scl(48f) / window.getHeight(), 0.4f),
                                        Actions.fadeIn(0.4f)
                                ),
                                Actions.visible(false)
                        )
                    );

                    break;
                }
            }
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

        if (flowIconMap.get(window) == null) {
            Table flowIcon = new Table(Styles.black6);
            flowIcon.button(window.getIcon(), Styles.clearNoneTogglei, 32f, () -> {
                if (window.visible)
                    return;

                window.translation.set(flowIcon.x, flowIcon.y).add(flowIcon.getWidth() / 2f, flowIcon.getHeight() / 2f);
                window.translation.sub(window.x, window.y).sub(window.originX, window.originY);
                window.setScale(flowIcon.getWidth() / window.getWidth(), flowIcon.getHeight() / window.getHeight());
                window.color.a(0f);
                window.touchable = Touchable.disabled;
                window.visible = true;

                window.actions(
                        Actions.sequence(
                                Actions.parallel(
                                        Actions.scaleTo(1f, 1f, 0.4f),
                                        Actions.translateBy(- window.translation.x, - window.translation.y, 0.4f),
                                        Actions.fadeIn(0.4f)
                                ),
                                Actions.touchable(Touchable.enabled)
                        )
                );

                flowIcon.actions(
                        Actions.sequence(
                                Actions.fadeOut(0.4f),
                                Actions.visible(false)
                        )
                );
            }).size(48f);

            flowIconMap.put(window, flowIcon);
        }

        window.closed(() -> {
            removeWindow(window);
            flowIconMap.get(window).remove();
        });
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
