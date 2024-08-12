package finalCampaign.feature.featureClass.control.setMode;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.struct.*;
import arc.util.*;
import finalCampaign.feature.featureClass.binding.*;
import finalCampaign.feature.featureClass.fcDesktopInput.*;
import finalCampaign.feature.featureClass.tuner.*;
import finalCampaign.patch.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.type.*;
import mindustry.world.*;

public class fSetMode {
    private static boolean enabled;
    private static boolean isOn;
    private static setModeFragment frag;

    protected static boolean selecting;
    protected static boolean deselect;
    private static float x, y, w, h;
    private static float dx, dy, dw, dh;
    private static Color selectRectColor;
    private static Color deselectRectColor;

    protected static Seq<Building> selected;
    protected static Seq<Building> selectingBuilding;
    protected static Seq<Category> selectFilter;
    protected static boolean selectSameBlockBuilding;
    private static int selectedNumDelta;
    private static Seq<Team> activeTeams;
    private static Seq<Building> tmp;

    public static void init() {
        enabled = false;
        isOn = false;
        selecting = false;
        deselect = false;
        x = y = w = h = 0;
        dx = dy = dw = dh = 0;
        selected = new Seq<>();
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

        Events.on(WorldLoadEvent.class, e -> {
            activeTeams = Reflect.get(Vars.indexer, "activeTeams");
        });

        fFcDesktopInput.addBindingHandle(() -> {
            if (!enabled) return;
            if (Core.scene.hasDialog() || Core.scene.hasField()) return;

            if (Core.input.keyTap(binding.setMode)) {
                isOn = !isOn;
                if (isOn) frag.rebuild();
            }

            if (Vars.control.input.commandMode) isOn = false;
            if ((Core.input.keyDown(Binding.break_block) || Core.input.keyDown(Binding.schematic_select) || Core.input.keyDown(Binding.deselect) || Core.input.keyDown(Binding.pick) || Core.input.keyDown(Binding.pickupCargo)) && !Core.scene.hasMouse()) isOn = false;

            if (!isOn) selecting = false;

            if (Core.input.keyDown(KeyCode.mouseLeft) && isOn && !Core.scene.hasMouse()) {
                if (!selecting) {
                    selecting = true;
                    x = Core.input.mouseWorldX();
                    y = Core.input.mouseWorldY();
                    selectedNumDelta = 0;
                    tmp.clear();
                    selectingBuilding.clear();
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

                    for (Team team : activeTeams) {
                        tmp.clear();
                        selectingBuilding.clear();

                        team.data().buildingTree.intersect(x, y, w, h, tmp);
                        for (Building b :tmp) {
                            if (!selectFilter.contains(b.block.category) && selectFilter.size > 0) continue;
                            if (!deselect) {
                                if (b.block.privileged && !Vars.state.isEditor() && Vars.state.rules.mode() != Gamemode.sandbox) continue;
                                if (b.team != Vars.player.team() && !Vars.state.isEditor() && Vars.state.rules.mode() != Gamemode.sandbox) continue;
                            }
                            selectingBuilding.add(b);
                        }
                    }
                }
            }

            if (Core.input.keyRelease(KeyCode.mouseLeft) && selecting && isOn) {
                selecting = false;
                x = y = w = h = 0f;
                dx = dy = dw = dh = 0;

                for (Building b : selectingBuilding) {
                    IFcBuilding fb = (IFcBuilding) b;
                    if (deselect) {
                        if (!fb.fcSetModeSelected()) continue;
                        selected.remove(b);
                        selectingBuilding.add(b);
                        fb.fcSetModeSelected(false);
                        selectedNumDelta --;
                    } else {
                        if (fb.fcSetModeSelected()) continue;
                        selected.add(b);
                        fb.fcSetModeSelected(true);
                        selectedNumDelta ++;
                    }
                }
                selectingBuilding.clear();

                if (selectSameBlockBuilding && selected.size >= 1 && selectedNumDelta == 1 && !deselect) {
                    Seq<Building> stack = new Seq<>();
                    stack.add(selected.pop());
                    tmp.clear();

                    while (stack.size > 0) {
                        Seq<Building> toAdd = new Seq<>();
                        for (Building b : stack) {
                            if (tmp.contains(b)) continue;

                            IFcBuilding fb = (IFcBuilding) b;
                            Tile t = b.tile();
                            if (!fb.fcSetModeSelected()) fb.fcSetModeSelected(!deselect);
                            int da = (int) (Math.ceil(b.block.size / 2f - 1f)) + 1;
                            int db = b.block.size % 2 == 0 ? da + 1 : da;

                            Building[] lst = new Building[4];
                            lst[0] = Vars.world.build(t.x, t.y - da);
                            lst[1] = Vars.world.build(t.x, t.y + db);
                            lst[2] = Vars.world.build(t.x - da, t.y);
                            lst[3] = Vars.world.build(t.x + db, t.y);

                            for (Building s : lst) if (s != null) if (s.block == b.block && s.team == b.team) toAdd.add(s);
                        }
                        tmp.add(stack);
                        stack = toAdd;
                    }

                    if (!deselect) {
                        selected.addAll(tmp);
                    } else {
                        selected.removeAll(tmp);
                    }
                }

                if (selectedNumDelta == 0) frag.forceSelectOpt = !frag.forceSelectOpt;
                if (selectedNumDelta != 0) frag.forceSelectOpt = false;

                frag.rebuild();
            }

            if (!isOn) {
                for (Building b : selected) ((IFcBuilding) b).fcSetModeSelected(false);
                selected.clear();
                selectingBuilding.clear();
                selectedNumDelta = 0;
                selecting = false;
            }
        });

        fFcDesktopInput.addDrawTopHandle(() -> {
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
                Lines.stroke(1f);
                Lines.circle(cx, cy, 1.44f);

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
                draw.get(b);
            }

            if (!deselect) for (Building b : selectingBuilding) draw.get(b);

            if (selecting) {
                Draw.color(deselect ? deselectRectColor : selectRectColor);
                Fill.crect(dx, dy, dw, dh);
                Draw.color();
            }
        });

        Vars.ui.hudGroup.fill(full -> {
            full.bottom().right();
            full.add(frag).right().bottom();
        });
    }

    public static boolean isOn() {
        return isOn && enabled;
    }

    public static void addFeature(iFeature feature) {
        if (!frag.categories.contains(feature.category)) frag.categories.add(feature.category);
        if (!frag.features.contains(feature)) frag.features.add(feature);
    }

    public static void addActiveTeam(Team team) {
        if (!activeTeams.contains(team)) activeTeams.add(team);
    }
}
