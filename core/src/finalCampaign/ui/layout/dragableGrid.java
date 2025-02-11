package finalCampaign.ui.layout;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.gen.*;

public class dragableGrid extends WidgetGroup {

    private int columns;
    private float elementWidth, elementHeight;
    private float elementMargin;
    private ObjectMap<dragableGrid, Func<Element, Element>> acceptTransformer;
    private ObjectMap<dragableGrid, Boolf<Element>> acceptFilter;
    private Seq<Element> order;
    private Seq<Runnable> modifiedListeners;
    private Element draggingElement;

    public dragableGrid(int columns, float elementWidth, float elementHeight, float elementMargin) {
        this.columns = columns;
        this.elementWidth = Scl.scl(elementWidth);
        this.elementHeight = Scl.scl(elementHeight);
        this.elementMargin = Scl.scl(elementMargin);

        touchable = Touchable.enabled;

        acceptTransformer = new ObjectMap<>();
        acceptFilter = new ObjectMap<>();
        order = new Seq<>();
        modifiedListeners = new Seq<>();
    }

    public void acceptFrom(dragableGrid src) {
        acceptFrom(src, e -> true);
    }

    public void acceptFrom(dragableGrid src, Boolf<Element> filter) {
        acceptFrom(src, filter, e -> e);
    }

    public void acceptFrom(dragableGrid src, Boolf<Element> filter, Func<Element, Element> transformer) {
        if (src == this)
            return;

        acceptTransformer.put(src, transformer);
        acceptFilter.put(src, filter);
    }

    protected boolean accept(dragableGrid src, Element e) {
        var filter = acceptFilter.get(src);
        var transformer = acceptTransformer.get(src);

        if (filter == null || transformer == null)
            return false;
        if (!filter.get(e))
            return false;

        dragEventHandle handle = null;

        for (var h : e.getListeners())
            if (h instanceof dragEventHandle deh)
                handle = deh;

        if (handle == null)
            return false;

        e.removeListener(handle);
        e = transformer.get(e);
        e.addListener(handle);
        addChild(e);

        draggingElement = e;
        invalidate();

        return true;
    }

    protected void processNewChild(Element e) {
        for (var h : e.getListeners())
            if (h instanceof dragEventHandle)
                return;

        e.addListener(new dragEventHandle(this, e));
    }

    @Override
    public void layout() {
        float cx, cy;
        int cols = 0;
        cx = cy = elementMargin;

        float realX = draggingElement == null ? 0f : draggingElement.x + draggingElement.translation.x;
        float realY = draggingElement == null ? 0f : draggingElement.y + draggingElement.translation.y;
        float minWidth = elementWidth * 0.5f;
        float minHeight = elementHeight * 0.5f;
        boolean inserted = false;

        order.clear();

        for (int i = 0; i < children.size; i++) {
            Element e = children.get(i);

            if (e == draggingElement)
                continue;

            // try insert here
            if (!inserted && draggingElement != null) {
                // overlaps each others
                if (realX < cx + elementWidth && realX + elementWidth > cx && realY < cy + elementHeight && realY + elementHeight > cy) {
                    // overlap's width or height is over half of element's width or height
                    if (elementWidth - Math.abs(realX - cx) > minWidth || elementHeight - Math.abs(realY - cy) > minHeight) {
                        e = draggingElement;
                        inserted = true;
                        i--;
                    }
                }
            }

            e.setSize(elementWidth, elementHeight);
            e.setPosition(cx, cy);
            order.add(e);

            if ((++ cols) % columns == 0) {
                cy += elementHeight + elementMargin;
                cx = elementMargin;
            } else {
                cx += elementWidth + elementMargin;
            }
        }

        // insert at last
        if (!inserted && draggingElement != null) {
            draggingElement.setSize(elementWidth, elementHeight);
            draggingElement.setPosition(cx, cy);
            order.add(draggingElement);
        }
    }

    @Override
    public void draw() {
        Draw.alpha(parentAlpha);

        if (draggingElement != null) {
            float realX = x + draggingElement.x;
            float realY = y + draggingElement.y;
            Tex.pane.draw(realX, realY, elementWidth, elementHeight);
        }

        super.draw();
    }

    @Override
    public void addChild(Element actor) {
        processNewChild(actor);
        super.addChild(actor);
    }

    @Override
    public void addChildAt(int index, Element actor) {
        processNewChild(actor);
        super.addChildAt(index, actor);
    }

    @Override
    public void addChildBefore(Element actorBefore, Element actor) {
        processNewChild(actor);
        super.addChildBefore(actorBefore, actor);
    }

    @Override
    public void addChildAfter(Element actorAfter, Element actor) {
        processNewChild(actor);
        super.addChildAfter(actorAfter, actor);
    }

    @Override
    public float getPrefWidth() {
        return 2f * elementMargin + columns * elementWidth;
    }

    @Override
    public float getPrefHeight() {
        return 2f * elementMargin + Math.max(1f, (float) Math.ceil(children.size / (float) columns)) * elementHeight;
    }

    public boolean isDragging() {
        return draggingElement != null;
    }

    public void modified(Runnable listener) {
        if (!modifiedListeners.contains(listener))
            modifiedListeners.add(listener);
    }

    public static class dragEventHandle extends InputListener {

        private dragableGrid grid;
        private Element e;
        private int dragPointer = -1;
        private KeyCode dragButton;
        private Vec2 tmp = new Vec2();
        private Element lastTarget;
        private dragableGrid lastPassTarget;

        public dragEventHandle(dragableGrid parent, Element e) {
            grid = parent;
            this.e = e;
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
            if (dragPointer != -1)
                return false;

            grid.draggingElement = e;
            dragPointer = pointer;
            dragButton = button;

            e.toFront();

            grid.invalidate();
            return true;
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            if (pointer != dragPointer)
                return;

            Element target = Core.scene.hit(x, y, true);
            if (lastTarget != target) {
                for (dragableGrid e : grid.acceptFilter.keys()) {
                    if (target == e || target.isDescendantOf(e)) {
                        if (lastPassTarget != e && e.accept(grid, grid.draggingElement)) {
                            grid.draggingElement = null;
                            grid.invalidate();
                            grid = e;
                            lastPassTarget = e;
                        }
                        break;
                    }
                }
                lastTarget = target;
            }

            e.localToParentCoordinates(tmp.set(x, y));
            e.translation.set(tmp.x - e.x, tmp.y - e.y);

            grid.invalidate();
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
            if (pointer != dragPointer || button != dragButton)
                return;

            dragPointer = -1;
            e.translation.setZero();

            grid.children.set(grid.order);
            grid.draggingElement = null;
            for (var listener : grid.modifiedListeners)
                listener.run();
        }
    }
}
