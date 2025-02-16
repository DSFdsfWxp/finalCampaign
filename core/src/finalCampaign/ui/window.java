package finalCampaign.ui;

import arc.*;
import arc.func.*;
import arc.input.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import finalCampaign.*;
import finalCampaign.ui.event.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;

public class window extends Table {

    public final Table cont;

    private Drawable icon;
    private Prov<CharSequence> title;
    private boolean movable;

    public Image iconImage;
    public Label titleLabel;
    public Table titleButtons;

    private boolean isShown;
    private Seq<Runnable> shownListeners, closedListeners;
    private Seq<Runnable> focusedListeners, bluredListeners;


    public window() {
        this(Icon.info, "Window");
    }

    public window(Drawable icon, String title) {
        this(icon, () -> title);
    }

    public window(Drawable icon, Prov<CharSequence> title) {
        this.icon = icon;
        this.title = title;
        movable = true;
        isShown = false;

        setBackground(Tex.pane);
        setTransform(true);
        setOrigin(Align.center);
        color.a(0f);

        table(titleBar -> {
            iconImage = titleBar.image(icon).size(48f).pad(2f).touchable(Touchable.enabled).get();
            titleLabel = titleBar.label(title).expandX().left().get();
            titleButtons = titleBar.table().pad(2f).touchable(Touchable.enabled).get();

            titleBar.addListener(new fcDragListener() {
                @Override
                public void drag(InputEvent event, float x, float y, int pointer) {
                    if (movable && Core.input.keyDown(KeyCode.mouseLeft))
                        setPosition(x, y);
                }
            });
        }).growX().row();
        image().color(Pal.gray).height(4f).growX().row();
        cont = table().grow().get();

        shownListeners = new Seq<>();
        closedListeners = new Seq<>();
        focusedListeners = new Seq<>();
        bluredListeners = new Seq<>();
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
        iconImage.setDrawable(icon);
    }

    public String getTitle() {
        return title.get().toString();
    }

    public void setTitle(Prov<CharSequence> title) {
        this.title = title;
        titleLabel.setText(title);
    }

    public void setTitle(String title) {
        setTitle(() -> title);
    }

    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    public void shown(Runnable listener) {
        if (!shownListeners.contains(listener))
            shownListeners.add(listener);
    }

    public void closed(Runnable listener) {
        if (!closedListeners.contains(listener))
            closedListeners.add(listener);
    }

    public void focused(Runnable listener) {
        if (!focusedListeners.contains(listener))
            focusedListeners.add(listener);
    }

    public void blured(Runnable listener) {
        if (!bluredListeners.contains(listener))
            bluredListeners.add(listener);
    }

    public void show() {
        clearActions();
        setScale(0.8f);
        color.a(0f);

        actions(
                Actions.sequence(
                        Actions.parallel(
                                Actions.scaleTo(1f, 1f, 0.4f),
                                Actions.fadeIn(0.4f)
                        ),
                        Actions.run(() -> {
                            touchable = Touchable.enabled;
                            fire(shownListeners);
                        })
                )
        );

        isShown = true;
    }

    public void close() {
        clearActions();
        Core.scene.setKeyboardFocus(null);

        actions(
                Actions.sequence(
                        Actions.run(() -> touchable = Touchable.disabled),
                        Actions.parallel(
                                Actions.scaleTo(0.8f, 0.8f, 0.4f),
                                Actions.fadeOut(0.4f)
                        ),
                        Actions.run(() -> fire(closedListeners))
                )
        );

        isShown = false;
    }

    public void focus() {
        titleLabel.clearActions();
        iconImage.clearActions();
        titleButtons.clearActions();

        titleLabel.actions(Actions.alpha(1f, 0.2f));
        iconImage.actions(Actions.alpha(1f, 0.2f));
        titleButtons.actions(Actions.alpha(1f, 0.2f));

        actions(
                Actions.sequence(
                        Actions.delay(0.2f),
                        Actions.run(() -> fire(focusedListeners))
                )
        );
    }

    public void blur() {
        titleLabel.clearActions();
        iconImage.clearActions();
        titleButtons.clearActions();

        Core.scene.setKeyboardFocus(null);

        titleLabel.actions(Actions.alpha(0.8f, 0.2f));
        iconImage.actions(Actions.alpha(0.8f, 0.2f));
        titleButtons.actions(Actions.alpha(0.8f, 0.2f));

        actions(
                Actions.sequence(
                        Actions.delay(0.2f),
                        Actions.run(() -> fire(bluredListeners))
                )
        );
    }

    public boolean isShown() {
        return isShown;
    }

    private void fire(Seq<Runnable> listeners) {
        for (var r : listeners)
            r.run();
    }

    public void addTitleBarButton(Drawable icon, Runnable handle) {
        addTitleBarButton(icon, "", v -> {
            handle.run();
            return false;
        });
    }

    public void addTitleBarButton(Drawable icon, Boolf<Boolean> handle) {
        addTitleBarButton(icon, "", handle);
    }

    public void addTitleBarButton(Drawable icon, Prov<Boolean> enabled, Runnable handle) {
        addTitleBarButton(icon, null, enabled, v -> {
            handle.run();
            return false;
        });
    }

    public void addTitleBarButton(Drawable icon, Prov<Boolean> enabled, Boolf<Boolean> handle) {
        addTitleBarButton(icon, null, enabled, handle);
    }

    public void addTitleBarButton(Drawable icon, String toolTip, Runnable handle) {
        addTitleBarButton(icon, toolTip, () -> true, v -> {
            handle.run();
            return false;
        });
    }

    public void addTitleBarButton(Drawable icon, String toolTip, Boolf<Boolean> handle) {
        addTitleBarButton(icon, toolTip, () -> true, handle);
    }

    public void addTitleBarButton(Drawable icon, String toolTip, Prov<Boolean> enabled, Runnable handle) {
        addTitleBarButton(icon, toolTip, enabled, v -> {
            handle.run();
            return false;
        });
    }

    public void addTitleBarButton(Drawable icon, String toolTip, Prov<Boolean> enabled, Boolf<Boolean> handle) {
        ImageButton button = new ImageButton(icon, Styles.clearNoneTogglei);
        var cell = titleButtons.add(button).size(48f);

        button.resizeImage(32f);
        button.clicked(() -> {
            if (enabled.get())
                button.setChecked(handle.get(button.isChecked()));
        });

        if (toolTip != null && !toolTip.isEmpty())
            cell.tooltip(toolTip);

        cell.update(i -> {
            i.color.a(enabled.get() ? 1f: 0.8f);
        });
    }

    public void addCloseButton() {
        addCloseButton(true);
    }

    public void addCloseButton(boolean hasToolTip) {
        addTitleBarButton(Icon.cancel, hasToolTip ? bundle.get("close") : "", this::close);
    }
}
