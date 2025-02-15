package finalCampaign.feature.featureBar;

import arc.func.*;
import arc.input.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import finalCampaign.*;
import finalCampaign.feature.hudUI.*;
import finalCampaign.ui.*;
import finalCampaign.ui.layout.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;

public class ui {

    protected static Table bar;
    protected static Cell<Table> barCell;
    protected static window moreWindow;
    protected static Seq<fFeatureBar.featureButton> buttons;

    private static ObjectMap<fFeatureBar.featureButton, Boolean> lastValidButtonMap;
    private static fFeatureBar.featureButton[] shownOnBarButtonOrder;
    private static Seq<fFeatureBar.featureButton> shownOnBarButtons;
    private static boolean moreWindowEditing;

    public static void init() {
        bar = new Table(Tex.pane);
        bar.visible(() -> fFeatureBar.enabled);
        bar.update(ui::updateBar);

        moreWindow = new window(Icon.menu, bundle.get("featureBar.more.title.normal"));
        moreWindow.name = "featureBar.moreWindow";
        moreWindow.addCloseButton();
        moreWindow.addTitleBarButton(Icon.pencil, bundle.get("edit"), editing -> {
            moreWindowEditing = !editing;
            rebuildWindow();
            moreWindow.setTitle(moreWindowEditing ? bundle.get("featureBar.more.title.edit") : bundle.get("featureBar.more.title.normal"));
            return !editing;
        });
        moreWindow.update(ui::updateWindow);

        buttons = new Seq<>();
        lastValidButtonMap = new ObjectMap<>();
        shownOnBarButtonOrder = new fFeatureBar.featureButton[10];
        shownOnBarButtons = new Seq<>();
        moreWindowEditing = false;
    }

    public static void buildBarUI() {
        barCell = fHudUI.fixedLayer.bottomLeft.add(bar).pad(4f).padRight(0f).marginLeft(-4f).marginBottom(-4f);
    }

    public static void updateBarUIVisible() {
        if (barCell != null)
            barCell.setElement(fFeatureBar.enabled ? bar : null);
    }

    public static void setup() {
        updateButtonValid();
        rebuildBar();
        if (moreWindow.isShown())
            rebuildWindow();
    }

    public static Cell<ImageButton> buildMoreWindowToggleButton(Table parent, ImageButton.ImageButtonStyle style, float size) {
        return parent.button(Icon.settings, style, size, () -> {
            if (moreWindow.isShown()) {
                moreWindow.close();
            }
            else {
                rebuildWindow();
                moreWindow.setPosition(Scl.scl(4f), bar.getHeight() + Scl.scl(4f));
                fHudUI.windowLayer.showWindow(moreWindow, fFeatureBar.config.rememberMoreWindowPosition);
            }
        }).update(ib -> ib.setChecked(moreWindow.isShown()));
    }

    private static void rebuildBar() {
        int cnt = 0;

        for (int i = 0; i < 10; i++)
            shownOnBarButtonOrder[i] = null;
        shownOnBarButtons.clear();
        bar.clear();
        bar.bottom();

        for (var button : buttons) {
            if (button.isShownOnBar()) {
                shownOnBarButtonOrder[button.getShowIndexOnBar()] = button;
                shownOnBarButtons.add(button);
            }
        }

        for (var button : shownOnBarButtonOrder) {
            if (button != null) {
                if (button.isValid()) {
                    bar.add(button.buildButton()).marginRight(4f).get().resizeImage(48f);
                    if (Vars.mobile && ++cnt % 5 == 0)
                        bar.row();
                }
            }
        }

        if (!Vars.mobile)
            buildMoreWindowToggleButton(bar, Styles.clearTogglei, 48f);
    }

    private static void rebuildWindow() {
        moreWindow.cont.clear();

        draggableGrid onBarButtons = new draggableGrid(5, 48f, 48f, 4f);
        draggableGrid otherButtons = new draggableGrid(5, 48f, 48f, 4f);

        Runnable updateOnBarButtonOrder = () -> {
            var children = onBarButtons.getChildren();

            for (int i = 0; i < children.size; i++) {
                var child = children.get(i);

                if (child instanceof fFeatureBar.featureImageButton fib) {
                    fib.getFeatureButton().setShowIndexOnBar(i);
                }
            }

            for (var child : otherButtons.getChildren())
                if (child instanceof fFeatureBar.featureImageButton fib)
                    fib.getFeatureButton().setShowIndexOnBar(-1);

            rebuildBar();
        };

        onBarButtons.setDraggable(moreWindowEditing);
        otherButtons.setDraggable(moreWindowEditing);
        onBarButtons.acceptFrom(otherButtons, e -> shownOnBarButtons.size < 10);
        otherButtons.acceptFrom(onBarButtons);
        onBarButtons.modified(updateOnBarButtonOrder);
        otherButtons.modified(updateOnBarButtonOrder);

        Cons2<draggableGrid, fFeatureBar.featureButton> processButton = (grid, button) -> {
            if (button.isValid() || moreWindowEditing) {
                ImageButton ib = button.buildButton();
                ib.resizeImage(48f);

                ib.clicked(moreWindowEditing ? KeyCode.mouseLeft : KeyCode.mouseRight, () -> {
                    draggableGrid target = ib.parent == onBarButtons ? otherButtons : onBarButtons;
                    ((draggableGrid) ib.parent).cancelDrag();

                    if (target == onBarButtons && shownOnBarButtons.size >= 10)
                        return;

                    ib.remove();
                    target.addChild(ib);
                    updateOnBarButtonOrder.run();
                });

                grid.addChild(ib);
            }
        };

        for (var button : shownOnBarButtonOrder) {
            if (button != null)
                processButton.get(onBarButtons, button);
        }

        for (var button : buttons) {
            if (!button.isShownOnBar())
                processButton.get(otherButtons, button);
        }

        if (Vars.mobile) {
            moreWindow.cont.add(otherButtons).row();
            moreWindow.cont.image().color(Pal.gray).height(4f).growX().row();
            moreWindow.cont.add(onBarButtons);
        } else {
            moreWindow.cont.add(onBarButtons).row();
            moreWindow.cont.image().color(Pal.gray).height(4f).growX().row();
            moreWindow.cont.add(otherButtons);
        }

    }

    private static void updateButtonValid() {
        for (var button : buttons)
            lastValidButtonMap.put(button, button.isValid());
    }

    private static void updateBar() {
        boolean needRebuild = false;

        for (var button : shownOnBarButtons) {
            if (button.isValid() != lastValidButtonMap.get(button)) {
                needRebuild = true;
                break;
            }
        }

        if (needRebuild)
            rebuildBar();

        // bar is always updating
        updateButtonValid();
    }

    private static void updateWindow() {
        if (!fFeatureBar.enabled && moreWindow.isShown()) {
            moreWindow.close();
            return;
        }

        if (!moreWindowEditing) {
            boolean needRebuild = false;

            for (var button : buttons) {
                if (button.isValid() != lastValidButtonMap.get(button)) {
                    needRebuild = true;
                    break;
                }
            }

            if (needRebuild)
                rebuildWindow();
        }
    }
}
