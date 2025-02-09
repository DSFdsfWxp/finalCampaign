package finalCampaign.ui;

import arc.*;
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

public class roulette extends Table {
    private TextureRegion base;
    private TextureRegion focus;
    private ObjectMap<Image, Object> map;
    private boolean needToLayout = false;
    private boolean removed = true;
    private Vec2 mouse;

    public boolean usingFourSlot = false;
    public int currentPage;
    public int totalPage;

    public Image selectedChild;
    public Object selectedObject;

    public roulette() {
        currentPage = totalPage = 0;
        map = new ObjectMap<>();
        mouse = new Vec2();
        touchable = Touchable.enabled;
        setSize(Scl.scl(Mathf.round(Math.min(Core.graphics.getHeight(), Core.graphics.getWidth()) * 0.1778f)));
    }

    public void setUsingFourSlot(boolean v) {
        if (v != usingFourSlot) {
            usingFourSlot = v;
            base = focus = null;
        }
    }

    public void addRouletteChild(Image child, Object objToStandFor) {
        int page = totalPage;

        fill(t -> {
            t.add(child);
            t.visible(() -> currentPage == page);
        });

        if (objToStandFor != null)
            map.put(child, objToStandFor);
        totalPage = Mathf.ceil((float) children.size / (usingFourSlot ? 4f : 8f));
        needToLayout = true;
    }

    public void clearRoulette() {
        currentPage = totalPage = 0;
        selectedChild = null;
        selectedObject = null;
        needToLayout = false;
        clear();
    }

    private void rouletteLayout() {
        int len = usingFourSlot ? 4 : 8;
        float unitDeg = 360f / len;

        Vec2 iPos = new Vec2();

        for (int i = currentPage * len, j = 0; j < len; i++, j++) {
            Image e = (Image) ((Table) children.get(i)).getChildren().get(0);
            e.setSize(width / 272f * 36f);
            iPos.set(0f, width * 0.3456f);
            iPos.rotate(90 + (len - 0.5f) * unitDeg);
            e.setTranslation(iPos.x, iPos.y);
        }

        needToLayout = false;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        x = mouse.x - width / 2f;
        y = mouse.y - width / 2f;
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

        if (pos.len() >= width * 0.1949f && r >= 0) {
            int index = r / rStep;

            selectedChild = (Image) ((Table) children.get(currentPage * len + index)).getChildren().get(0);
            selectedObject = map.get(selectedChild);

            Draw.rect(focus, cx, cy, width, width, (index + len / 4f) * rStep);
        } else {
            Draw.rect(base, cx, cy, width, width);
        }
    }

    @Override
    public void draw() {
        if (needToLayout)
            rouletteLayout();
        super.draw();
    }

    public void showRoulette(Group parent) {
        if (!removed)
            return;

        mouse.set(Core.input.mouseX(), Core.input.mouseY());
        setScale(0f);
        parent.fill(t -> {
            t.bottom().left();
            t.add(this);
        });

        actions(
                Actions.sequence(
                        Actions.fadeIn(0.4f),
                        Actions.scaleTo(1f, 1f, 0.4f),
                        Actions.run(() -> removed = false)
                )
        );
    }

    public void closeRoulette() {
        if (removed)
            return;

        removed = true;
        actions(
                Actions.sequence(
                        Actions.parallel(
                                Actions.fadeOut(0.4f),
                                Actions.scaleTo(0f, 0f, 0.4f)
                        ),
                        Actions.run(() -> {
                            parent.remove();
                            remove();
                        })
                )
        );
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

    public void addRoulettePageSwitchingHandle() {
        clicked(KeyCode.mouseLeft, () -> rouletteChangeToPage(currentPage + 1));
        clicked(KeyCode.mouseRight, () -> rouletteChangeToPage(currentPage - 1));
    }
}
