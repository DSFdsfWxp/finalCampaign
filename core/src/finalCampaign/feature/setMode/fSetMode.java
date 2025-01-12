package finalCampaign.feature.setMode;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.struct.*;
import finalCampaign.event.*;
import finalCampaign.feature.tuner.*;
import finalCampaign.input.*;
import finalCampaign.input.fcInputHook.*;
import finalCampaign.map.*;
import mindustry.*;
import mindustry.core.GameState.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.ConstructBlock.*;

public class fSetMode {
    private static boolean enabled;
    private static boolean isOn;
    private static setModeFragment frag;

    protected static boolean selecting;
    protected static boolean deselect;
    private static boolean mouseDown;
    @SuppressWarnings("unused")
    private static float mx, my;
    private static float x, y, w, h;
    private static float dx, dy, dw, dh;
    private static Color selectRectColor;
    private static Color deselectRectColor;

    protected static Seq<Building> selected;
    protected static Seq<Block> selectedBlock;
    protected static Seq<Building> selectingBuilding;
    protected static Seq<Category> selectFilter;
    protected static boolean selectSameBlockBuilding;
    private static int selectedNumDelta;
    private static Seq<Building> tmp;

    private static Building lastSelected;
    private static Building lastDeselected;

    public static boolean supported() {
        return !Vars.headless;
    }

    public static void init() {
        enabled = false;
        isOn = false;
        selecting = false;
        deselect = false;
        mouseDown = false;
        x = y = w = h = 0;
        mx = my = 0;
        dx = dy = dw = dh = 0;
        selected = new Seq<>();
        selectedBlock = new Seq<>();
        selectFilter = new Seq<>();
        selectingBuilding = new Seq<>();
        selectSameBlockBuilding = false;
        selectRectColor = Pal.accent.cpy().a(0.3f);
        deselectRectColor = Pal.health.cpy().a(0.3f);
        selectedNumDelta = 0;
        tmp = new Seq<>();
    }

    public static void load() {
        frag = new setModeFragment();
        uiPatcher.load();
        enabled = fTuner.add("setMode", false, v -> enabled = v);
        features.add();

        fcInputHook.installHook(inputHookPoint.pressed, fSetMode::keyHook);
        fcInputHook.installHook(inputHookPoint.tapped, fSetMode::keyHook);
        fcInputHook.installHook(inputHookPoint.released, fSetMode::keyHook);

        Events.on(StateChangeEvent.class, e -> {
            if (e.to == State.menu) {
                selected.clear();
                selectingBuilding.clear();
                tmp.clear();
                selecting = false;
                isOn = false;
                x = y = w = h = 0;
                dx = dy = dw = dh = 0;
            }
        });

        Events.on(fcInputHandleUpdateEvent.class, event -> {
            if (!enabled) return;
            if (Core.scene.hasDialog() || Core.scene.hasField()) return;

            if (Core.input.keyTap(fcBindings.setMode)) {
                isOn = !isOn;
                if (isOn) frag.rebuild();
            }

            if (Vars.control.input.commandMode || Vars.ui.minimapfrag.shown()) isOn = false;
            if (Vars.control.input.isDroppingItem() || Vars.control.input.config.isShown()) selecting = false;

            if (isOn) {
                Vars.control.input.block = null;
                Vars.control.input.selectPlans.clear();
            }

            if (fcInputHook.realIsPressed(KeyCode.mouseLeft) && isOn) {
                if (!mouseDown) {
                    mx = Core.input.mouseX();
                    my = Core.input.mouseY();
                    mouseDown = true;
                }
            }

            if ((fcInputHook.realIsPressed(Binding.select) || fcInputHook.realIsPressed(Binding.deselect)) && isOn && !Core.scene.hasMouse()) {
                if (!selecting && !Vars.player.dead()) {
                    if ((frag.forceSelectOpt || selected.size == 0 || (mx < frag.x)) && !Vars.control.input.isDroppingItem() && !Vars.control.input.config.isShown()) {
                        selecting = true;
                        x = Core.input.mouseWorldX();
                        y = Core.input.mouseWorldY();
                        selectedNumDelta = 0;
                        tmp.clear();
                        selectingBuilding.clear();
                        deselect = fcInputHook.realIsPressed(Binding.deselect);
                    }
                } else {
                    w = Core.input.mouseWorldX();
                    h = Core.input.mouseWorldY();
    
                    float x, y, w, h;
                    x = Math.min(fSetMode.x, fSetMode.w);
                    y = Math.min(fSetMode.y, fSetMode.h);
                    w = Math.max(fSetMode.x, fSetMode.w) - x;
                    h = Math.max(fSetMode.y, fSetMode.h) - y;

                    fSetMode.dx = x;
                    fSetMode.dy = y;
                    fSetMode.dw = w;
                    fSetMode.dh = h;

                    if (w <= 0) w = 1f;
                    if (h <= 0) h = 1f;

                    selectingBuilding.clear();
                    for (Team team : Team.all) {
                        if (team.data().buildingTree == null) continue;
                        if (!team.equals(Vars.player.team()) && !((Vars.state.rules.mode() == Gamemode.sandbox && fcMap.initialMode == null) || fcMap.initialMode == Gamemode.sandbox)) continue;
                        tmp.clear();

                        team.data().buildingTree.intersect(x, y, w, h, tmp);
                        for (Building b :tmp) {
                            if (b instanceof ConstructBuild) continue;
                            if (!selectFilter.contains(b.block.category) && selectFilter.size > 0) continue;
                            if (!deselect) {
                                if (b.block.privileged && !Vars.state.isEditor() && !((Vars.state.rules.mode() == Gamemode.sandbox && fcMap.initialMode == null) || fcMap.initialMode == Gamemode.sandbox)) continue;
                            }
                            selectingBuilding.add(b);
                        }
                    }
                }
            }

            if (fcInputHook.realIsReleased(KeyCode.mouseLeft) && mouseDown) {
                mx = my = 0;
                mouseDown = false;
            }

            if ((fcInputHook.realIsReleased(Binding.select) || fcInputHook.realIsReleased(Binding.deselect)) && selecting && isOn) {
                x = y = w = h = 0f;
                dx = dy = dw = dh = 0;

                for (Building b : selectingBuilding) {
                    if (deselect) {
                        selected.remove(b);
                        lastDeselected = b;
                        selectedNumDelta --;
                    } else {
                        if (selected.contains(b)) continue;
                        selected.add(b);
                        lastSelected = b;
                        selectedNumDelta ++;
                    }
                }
                selectingBuilding.clear();

                if (selectSameBlockBuilding && selected.size > 0 && Math.abs(selectedNumDelta) == 1) {
                    Seq<Building> stack = new Seq<>();
                    Seq<Building> nextStack = new Seq<>();
                    Building first = deselect ? lastDeselected : lastSelected;
                    stack.add(first);
                    tmp.clear();

                    while (stack.size > 0) {
                        nextStack.clear();
                        for (Building b : stack) {
                            if (tmp.contains(b)) continue;

                            Tile t = b.tile();
                            int da = (int) (Math.ceil(b.block.size / 2f - 1f)) + 1;
                            int db = b.block.size % 2 == 0 ? da + 1 : da;

                            Building[] lst = new Building[4];
                            lst[0] = Vars.world.build(t.x, t.y - da);
                            lst[1] = Vars.world.build(t.x, t.y + db);
                            lst[2] = Vars.world.build(t.x - da, t.y);
                            lst[3] = Vars.world.build(t.x + db, t.y);

                            for (Building s : lst) if (s != null) if (s.block == b.block && s.team == b.team) nextStack.add(s);
                            tmp.add(b);
                        }
                        Seq<Building> swap = stack;
                        stack = nextStack;
                        nextStack = swap;
                    }

                    if (!deselect) {
                        selected.pop();
                        selected.add(tmp);
                    } else {
                        selected.removeAll(tmp);
                    }
                }

                if (selectedNumDelta == 0) frag.forceSelectOpt = !frag.forceSelectOpt;
                if (selectedNumDelta != 0) {
                    frag.forceSelectOpt = false;
                    selectedBlock.clear();
                }

                selecting = false;
                frag.rebuild();
            }

            if (!isOn) {
                selected.clear();
                selectedBlock.clear();
                selectingBuilding.clear();
                selectedNumDelta = 0;
                selecting = false;
                frag.clear();
            }
        });

        Events.on(fcDrawWorldTopEvent.class, event -> {
            if (!isOn()) return;

            Cons<Building> draw = b -> {
                Tile t = b.tile();
                int size = b.block.size;
                if (!b.block.isMultiblock()) size = 1;
                float offset = size % 2 == 0 ? Vars.tilesize / 2f : 0f;

                float cx = t.x * Vars.tilesize + offset;
                float cy = t.y * Vars.tilesize + offset;
                float s = size * Vars.tilesize;

                float ta = s / 2f;
                float tb = s / 6f;

                Draw.color(Pal.accent);
                Draw.rect("circle", cx, cy, 1.94f, 1.94f);

                Lines.stroke(1.28f);

                Lines.line(cx - ta, cy - ta, cx - tb, cy - ta);
                Lines.line(cx - ta, cy - ta, cx - ta, cy - tb);

                Lines.line(cx + tb, cy - ta, cx + ta, cy - ta);
                Lines.line(cx + ta, cy - ta, cx + ta, cy - tb);

                Lines.line(cx + tb, cy + ta, cx + ta, cy + ta);
                Lines.line(cx + ta, cy + tb, cx + ta, cy + ta);

                Lines.line(cx - ta, cy + ta, cx - tb, cy + ta);
                Lines.line(cx - ta, cy + tb, cx - ta, cy + ta);

                Draw.reset();
            };

            for (Building b : selected) {
                if (deselect) if (selectingBuilding.contains(b)) continue;
                if (selectedBlock.size > 0 && !selectedBlock.contains(b.block)) continue;
                draw.get(b);
                if (b.dead() || b.tile().build != b) selected.remove(b);
            }

            if (!deselect) for (Building b : selectingBuilding) {
                draw.get(b);
                if (b.dead() || b.tile().build != b) selectingBuilding.remove(b);
            }

            if (selecting) {
                Draw.color(deselect ? deselectRectColor : selectRectColor);
                Fill.crect(dx, dy, dw, dh);
                Draw.color();
            }
        });

        Vars.ui.hudGroup.fill(full -> {
            full.bottom().right();
            full.add(frag).right().bottom().growY();
        });
    }

    public static boolean isOn() {
        return isOn && enabled;
    }

    private static boolean keyHook(KeyCode code, boolean v) {
        if (!isOn()) return v;
        if (code.equals(Core.keybinds.get(Binding.deselect).key) ||
            code.equals(Core.keybinds.get(Binding.break_block).key) ||
            code.equals(Core.keybinds.get(Binding.schematic_select).key) ||
            code.equals(Core.keybinds.get(Binding.rebuild_select).key))
                return false;
        return v;
    }

    /** Set whether the setting pane can be flick scrolled */
    public static void setFlickScrollEnabled(boolean v) {
        if (frag.pane != null) frag.pane.setFlickScroll(v);
    }

    public static void addFeature(IFeature feature) {
        if (!frag.categories.contains(feature.category)) frag.categories.add(feature.category);
        if (!frag.features.contains(feature)) frag.features.add(feature);
    }
}
