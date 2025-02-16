package finalCampaign.ui;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import finalCampaign.*;
import mindustry.ui.*;

public class roulette extends Table {
    private TextureRegion base;
    private TextureRegion focus;
    private ObjectMap<Image, rouletteChoicePair> pairMap;
    private ObjectMap<Image, Color> colorMap;
    private boolean needToLayout = false;
    private boolean removed = true;
    private Vec2 mouse;
    private Stack stack;
    private Label label;

    public boolean usingFourSlot = false;
    public int currentPage;
    public int totalPage;

    public rouletteChoicePair selected;

    public roulette() {
        currentPage = totalPage = 0;
        pairMap = new ObjectMap<>();
        colorMap = new ObjectMap<>();
        mouse = new Vec2();
        touchable = Touchable.enabled;
        setTransform(true);

        stack = stack().grow().get();
        row();
        table(t -> {
            t.background(Styles.black6);
            label = t.add("null").get();
        }).marginTop(4f);

        stack.setFillParent(true);
        stack.setTransform(true);

        float size = Scl.scl(Mathf.round(Math.min(Core.graphics.getHeight(), Core.graphics.getWidth()) * 0.1778f));
        setSize(size, size + Scl.scl(4f) + label.getPrefHeight());
        setOrigin(width / 2f, height - width / 2f);
    }

    public void setUsingFourSlot(boolean v) {
        if (v != usingFourSlot) {
            usingFourSlot = v;
            base = focus = null;
        }
    }

    public void addRouletteChoice(Image icon, Object obj, String displayName) {
        addRouletteChoice(icon, obj, displayName, () -> {});
    }

    public void addRouletteChoice(Image icon, Object obj, String displayName, Runnable handle) {
        addRouletteChoice(icon, obj, displayName, () -> true, handle);
    }

    public void addRouletteChoice(Image icon, Object obj, String displayName, Boolp valid, Runnable handle) {
        addRouletteChoice(icon, obj, () -> displayName, valid, handle);
    }

    public void addRouletteChoice(Image icon, Object obj, Prov<String> displayName) {
        addRouletteChoice(icon, obj, displayName, () -> true, () -> {});
    }

    public void addRouletteChoice(Image icon, Object obj, Prov<String> displayName, Runnable handle) {
        addRouletteChoice(icon, obj, displayName, () -> true, handle);
    }

    public void addRouletteChoice(Image icon, Object obj, Prov<String> displayName, Boolp valid, Runnable handle) {
        int page = totalPage;
        Table t = new Table();

        t.setFillParent(true);
        t.add(icon);
        t.visible(() -> currentPage == page);
        stack.add(t);

        rouletteChoicePair pair = new rouletteChoicePair();
        pair.icon = icon;
        pair.obj = obj;
        pair.displayName = displayName;
        pair.valid = valid;
        pair.handle = handle;
        pairMap.put(icon, pair);

        totalPage = Mathf.ceil((float) children.size / (usingFourSlot ? 4f : 8f));
        needToLayout = true;
    }

    public void clearRoulette() {
        currentPage = totalPage = 0;
        selected = null;
        needToLayout = false;
        pairMap.clear();
        colorMap.clear();
        stack.clear();
    }

    private void rouletteLayout() {
        int len = usingFourSlot ? 4 : 8;
        float unitDeg = 360f / len;

        Vec2 iPos = new Vec2();

        for (int k = 0; k < totalPage; k++) {
            for (int i = k * len, j = 0; j < len; i++, j++) {
                Image e = (Image) ((Table) stack.getChildren().get(i)).getChildren().get(0);
                e.setSize(width / 272f * 36f);
                iPos.set(0f, width * 0.3456f);
                iPos.rotate(90 + (len - 0.5f) * unitDeg * j);
                e.setTranslation(iPos.x, iPos.y);
            }
        }

        needToLayout = false;
    }

    @Override
    protected void drawBackground(float x, float y) {
        int len = usingFourSlot ? 4 : 8;

        if (base == null || focus == null) {
            base = atlas.find("roulette-" + len);
            focus = atlas.find("roulette-" + len + "-focus");
        }

        Vec2 pos = Core.input.mouse().sub(mouse);
        int r = -1;
        int rStep;
        float mr = pos.angle();

        if (usingFourSlot) {
            if ((mr < 45f && mr > 0f) || (mr < 360f && mr > 315f)) r = 270;
            if (mr < 135f && mr > 45f) r = 0;
            if (mr < 225f && mr > 135f) r = 90;
            if (mr < 315f && mr > 225f) r = 180;
            rStep = 90;
        } else {
            if ((mr < 22.5f && mr > 0f) || (mr < 360f && mr > 337.5f)) r = 270;
            if (mr < 67.5f && mr > 22.5f) r = 315;
            if (mr < 112.5f && mr > 67.5f) r = 0;
            if (mr < 157.5f && mr > 112.5f) r = 45;
            if (mr < 202.5f && mr > 157.5f) r = 90;
            if (mr < 247.5f && mr > 202.5f) r = 135;
            if (mr < 292.5f && mr > 247.5f) r = 180;
            if (mr < 337.5f && mr > 292.5f) r = 225;
            rStep = 45;
        }

        float cx = x + width / 2f;
        float cy = y + width / 2f;

        Draw.color(color.r, color.g, color.b, color.a * parentAlpha);

        int index = r / rStep;

        if (pos.len() >= width * 0.1949f && r >= 0 && currentPage * len + index < children.size) {
            Image selectedChild = (Image) ((Table) children.get(currentPage * len + index)).getChildren().get(0);
            rouletteChoicePair selected = pairMap.get(selectedChild);
            String selectedName = selected.displayName.get();

            if (!selectedName.isEmpty()) {
                label.parent.visible = true;
                label.setText(selected.displayName.get());
            } else {
                label.parent.visible = false;
            }

            if (selected.valid.get()) {
                this.selected = selected;
                Draw.rect(focus, cx, cy, width, width, (index + len / 4f) * rStep);
            } else {
                this.selected = null;
                Draw.rect(base, cx, cy, width, width);
            }
        } else {
            label.parent.visible = false;
            this.selected = null;
            Draw.rect(base, cx, cy, width, width);
        }
    }

    @Override
    public void draw() {
        if (needToLayout)
            rouletteLayout();

        for (int i = 0; i < children.size; i++) {
            Image e = (Image) ((Table) stack.getChildren().get(i)).getChildren().get(0);
            rouletteChoicePair pair = pairMap.get(e);

            colorMap.put(e, e.color);
            if (!pair.valid.get())
                e.color.mul(Color.darkGray);
        }

        super.draw();

        for (var entry : colorMap)
            entry.key.color.set(entry.value);
    }

    public void showRoulette(Group parent) {
        if (!removed)
            return;

        selected = null;
        removed = false;
        mouse.set(Core.input.mouseX(), Core.input.mouseY());
        setScale(0f);
        parent.fill(t -> {
            t.touchable = Touchable.enabled;
            t.bottom().left();
            t.add(this);
            t.getListeners().set(getListeners());
            getListeners().clear();
        });
        setPosition(mouse.x - width / 2f, mouse.y - height + width / 2f);
        parent.requestScroll();

        clearActions();
        actions(
                Actions.sequence(
                        Actions.fadeIn(0.4f),
                        Actions.scaleTo(1f, 1f, 0.4f)
                )
        );
    }

    public void closeRoulette() {
        if (removed)
            return;

        removed = true;
        clearActions();
        actions(
                Actions.sequence(
                        Actions.parallel(
                                Actions.fadeOut(0.4f),
                                Actions.scaleTo(0f, 0f, 0.4f)
                        ),
                        Actions.run(() -> {
                            Core.scene.setScrollFocus(null);
                            parent.remove();
                            remove();
                        })
                )
        );

        getListeners().set(parent.getListeners());
        parent.getListeners().clear();

        if (selected != null)
            selected.handle.run();
    }

    public boolean isShown() {
        return !removed;
    }

    public void rouletteWarn() {
        if (removed)
            return;

        clearActions();
        actions(
                Actions.sequence(
                        Actions.parallel(
                                Actions.color(Color.acid, 0.2f),
                                Actions.scaleTo(1.2f, 1.2f, 0.2f)
                        ),
                        Actions.parallel(
                                Actions.color(Color.white, 0.2f),
                                Actions.scaleTo(1f, 1f, 0.2f)
                        )
                )
        );
    }

    public void rouletteChangeToPage(int page) {
        if (removed)
            return;

        if (page < 0 || page >= totalPage) {
            rouletteWarn();
        } else {
            currentPage = page;
        }
    }

    public void addRouletteScrollPageSwitchingHandle() {
        scrolled(v -> {
            rouletteChangeToPage(currentPage + (int) v);
        });
    }

    public void addRouletteClickPageSwitchingHandle() {
        clicked(KeyCode.mouseLeft, () -> rouletteChangeToPage(currentPage + 1));
        clicked(KeyCode.mouseRight, () -> rouletteChangeToPage(currentPage - 1));
    }

    public static class rouletteChoicePair {
        public Image icon;
        public Object obj;
        public Prov<String> displayName;
        public Boolp valid;
        public Runnable handle;
    }
}
