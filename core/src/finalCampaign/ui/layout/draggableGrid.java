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

public class draggableGrid extends WidgetGroup {

    private int columns;
    private float elementWidth, elementHeight;
    private float elementMargin;
    private boolean draggable;
    private ObjectMap<draggableGrid, Func<Element, Element>> acceptTransformer;
    private ObjectMap<draggableGrid, Boolf<Element>> acceptFilter;
    private Seq<Element> order;
    private Seq<Runnable> modifiedListeners;
    private Element draggingElement;
    private dragEventHandle activeDraggingHandle;

    public draggableGrid(int columns, float elementWidth, float elementHeight, float elementMargin) {
        this.columns = columns;
        this.elementWidth = Scl.scl(elementWidth);
        this.elementHeight = Scl.scl(elementHeight);
        this.elementMargin = Scl.scl(elementMargin);

        draggable = true;
        touchable = Touchable.enabled;

        acceptTransformer = new ObjectMap<>();
        acceptFilter = new ObjectMap<>();
        order = new Seq<>();
        modifiedListeners = new Seq<>();
    }

    public void acceptFrom(draggableGrid src) {
        acceptFrom(src, e -> true);
    }

    public void acceptFrom(draggableGrid src, Boolf<Element> filter) {
        acceptFrom(src, filter, e -> e);
    }

    public void acceptFrom(draggableGrid src, Boolf<Element> filter, Func<Element, Element> transformer) {
        if (src == this)
            return;

        acceptTransformer.put(src, transformer);
        acceptFilter.put(src, filter);
    }

    protected boolean accept(draggableGrid src, Element e) {
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

        e = transformer.get(e);
        e.addListener(handle.clone(this));
        handle.cancel();
        addChild(e);

        draggingElement = e;
        invalidate();

        return true;
    }

    protected void processNewChild(Element e) {
        e.getListeners().remove((Boolf<EventListener>) h -> h instanceof dragEventHandle);
        e.addListener(new dragEventHandle(this, e));
    }

    @Override
    public void layout() {
        float cx = elementMargin, cy = getPrefHeight() - elementMargin;
        int cols = 0;

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
                cy -= elementHeight + elementMargin;
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
    public boolean removeChild(Element actor, boolean unfocus) {
        actor.getListeners().remove((Boolf<EventListener>) h -> h instanceof dragEventHandle);
        return super.removeChild(actor, unfocus);
    }

    @Override
    public void clearChildren() {
        for (var e : children)
            e.getListeners().remove((Boolf<EventListener>) h -> h instanceof dragEventHandle);
        super.clearChildren();
    }

    @Override
    public float getPrefWidth() {
        return 2f * elementMargin + columns * elementWidth;
    }

    @Override
    public float getPrefHeight() {
        return 2f * elementMargin + Math.max(1f, (float) Math.ceil(children.size / (float) columns)) * elementHeight;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public boolean isDragging() {
        return draggingElement != null;
    }

    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
        if (!draggable && activeDraggingHandle != null)
            activeDraggingHandle.cancel();
    }

    public void cancelDrag() {
        if (activeDraggingHandle == null)
            return;
        activeDraggingHandle.cancel();
    }

    public void modified(Runnable listener) {
        if (!modifiedListeners.contains(listener))
            modifiedListeners.add(listener);
    }

    public static class dragEventHandle extends InputListener {

        private draggableGrid grid;
        private Element e;
        private int dragPointer = -1;
        private KeyCode dragButton;
        private Vec2 tmp = new Vec2();
        private InputEvent lastEvent;
        private Element lastTarget;
        private draggableGrid lastPassTarget;

        public dragEventHandle(draggableGrid parent, Element e) {
            grid = parent;
            this.e = e;
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
            if (dragPointer != -1 || !grid.draggable)
                return false;

            grid.draggingElement = e;
            grid.activeDraggingHandle = this;
            dragPointer = pointer;
            dragButton = button;
            lastEvent = event;

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
                for (draggableGrid e : grid.acceptFilter.keys()) {
                    if (target == e || target.isDescendantOf(e)) {
                        if (lastPassTarget != e && e.accept(grid, grid.draggingElement)) {
                            grid.draggingElement = null;
                            grid.activeDraggingHandle = null;
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

            cancel();
            for (var listener : grid.modifiedListeners)
                listener.run();
        }

        public void cancel() {
            if (dragPointer == -1)
                return;

            dragPointer = -1;
            e.translation.setZero();

            grid.children.set(grid.order);
            grid.draggingElement = null;
            grid.activeDraggingHandle = null;
        }

        public dragEventHandle clone(draggableGrid target) {
            dragEventHandle res = new dragEventHandle(target, e);
            if (dragPointer != -1) {
                res.dragPointer = dragPointer;
                res.dragButton = dragButton;
                res.lastEvent = lastEvent;
                Core.scene.addTouchFocus(res, e, lastEvent.targetActor, dragPointer, dragButton);
            }
            return res;
        }
    }
}
