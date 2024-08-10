package finalCampaign.feature.featureClass.control.setMode;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.struct.*;
import finalCampaign.feature.featureClass.binding.*;
import finalCampaign.feature.featureClass.fcDesktopInput.*;
import finalCampaign.feature.featureClass.tuner.*;
import finalCampaign.patch.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.type.*;
import mindustry.world.*;

public class fSetMode {
    // reloadTurret -> Turret => TurretBuild.updateTile() -> TurretBuild.findTarget()
    // Turret -> LiquidTurret => TurretBuild.updateTile() -> LiquidTurretBuild.findTarget()

    // Building.control(LAccess type, Object p1, double p2, double p3, double p4)
    // add a field in Building : @Nullable Block fcCurrentLinkedLogicBlock
    // Plan A : mixin constructor LogicBlock() : config(int)
    // Plan B : mixin LogicBlock.getLinkName(...) and LogicBuild.findLinkName(...)

    // Building.updateConsumption()
    // TurretBuild.updateEfficiencyMultiplier()
    // add a field in Building : fcCheating
    // Building.cheating()

    private static boolean enabled;
    private static boolean isOn;
    private static setModeFragment frag;

    protected static boolean selecting;
    protected static boolean deselect;
    private static float x, y, w, h;
    private static Color selectRectColor;
    private static Color deselectRectColor;

    protected static Seq<Building> selected;
    protected static Seq<Category> selectFilter;
    protected static boolean selectSameBlockBuilding;
    private static int selectedNumDelta;

    public static void init() {
        enabled = false;
        isOn = false;
        selecting = false;
        deselect = false;
        x = y = w = h = 0;
        selected = new Seq<>();
        selectFilter = new Seq<>();
        selectSameBlockBuilding = false;
        selectRectColor = Pal.accent.cpy().a(0.3f);
        deselectRectColor = Pal.health.cpy().a(0.3f);
        selectedNumDelta = 0;
    }

    public static void load() {
        frag = new setModeFragment();
        uiPatcher.load();
        fTuner.add("setMode", false, v -> enabled = v);
        features.add();

        fFcDesktopInput.addBindingHandle(() -> {
            if (!enabled) return;
            if (Core.input.keyTap(binding.setMode)) {
                isOn = !isOn;
                if (isOn) frag.rebuild();
            }

            if (Vars.control.input.commandMode) isOn = false;
            if (Core.input.keyDown(Binding.break_block) || Core.input.keyDown(Binding.select) || Core.input.keyDown(Binding.deselect) || Core.input.keyDown(Binding.pick) || Core.input.keyDown(Binding.pickupCargo)) isOn = false;

            if (!isOn) selecting = false;

            if (Core.input.keyDown(KeyCode.mouseLeft) && !selecting && isOn) {
                if (!selecting) {
                    selecting = true;
                    x = Core.input.mouseWorldX();
                    y = Core.input.mouseWorldY();
                    selectedNumDelta = 0;
                } else {
                    float ox, oy, ow, oh;
                    ox = fSetMode.x;
                    oy = fSetMode.y;
                    ow = fSetMode.w;
                    oh = fSetMode.h;
                    
                    w = Core.input.mouseWorldX();
                    y = Core.input.mouseWorldY();
    
                    float x, y, w, h;
                    x = Math.min(fSetMode.x, fSetMode.w);
                    y = Math.min(fSetMode.y, fSetMode.h);
                    w = Math.max(fSetMode.x, fSetMode.w) - x;
                    h = Math.max(fSetMode.y, fSetMode.h) - y;

                    fSetMode.x = x;
                    fSetMode.y = y;
                    fSetMode.w = w;
                    fSetMode.h = h;

                    if (w <= 0) w = Vars.tilesize;
                    if (h <= 0) h = Vars.tilesize;
                    if (ow <=0) ow = Vars.tilesize;
                    if (oh <=0) oh = Vars.tilesize;

                    int wx, wy, ww, wh;
                    wx = (int)(x / Vars.tilesize);
                    wy = (int)(y / Vars.tilesize);
                    ww = (int)(w / Vars.tilesize);
                    wh = (int)(h / Vars.tilesize);

                    int owx, owy, oww, owh;
                    owx = (int)(ox / Vars.tilesize);
                    owy = (int)(oy / Vars.tilesize);
                    oww = (int)(ow / Vars.tilesize);
                    owh = (int)(oh / Vars.tilesize);

                    if (wx == owx && wy == owy && ww == oww && wh == owh) return;
                    
                    for (int py=0; py<wh; py++) {
                        for (int px=0; px<ww; px++) {
                            Building b = Vars.world.build(px + wx, py + wy);
                            if (b == null) continue;
                            if (!selectFilter.contains(b.block.category) && selectFilter.size > 0) continue;
                            IFcBuilding fb = (IFcBuilding) b;
                            if (deselect) {
                                if (!fb.fcSetModeSelected()) continue;
                                selected.remove(b);
                                fb.fcSetModeSelected(false);
                                selectedNumDelta --;
                            } else {
                                if (fb.fcSetModeSelected()) continue;
                                if (b.block.privileged && (!Vars.state.isEditor() || Vars.state.rules.mode() != Gamemode.sandbox)) continue;
                                if (b.team != Vars.player.team() && (!Vars.state.isEditor() || Vars.state.rules.mode() != Gamemode.sandbox)) continue;
                                selected.add(b);
                                fb.fcSetModeSelected(true);
                                selectedNumDelta ++;
                            }
                        }
                    }
                }
            }

            if (Core.input.keyRelease(KeyCode.mouseLeft) && selecting && isOn) {
                selecting = false;

                if (selectSameBlockBuilding && selected.size >= 1 && selectedNumDelta == 1 && !deselect) {
                    Seq<Building> stack = new Seq<>();
                    stack.add(selected.pop());

                    while (stack.size > 0) {
                        Seq<Building> toAdd = new Seq<>();
                        for (Building b : stack) {
                            IFcBuilding fb = (IFcBuilding) b;
                            Tile t = b.tile();
                            if (!fb.fcSetModeSelected()) fb.fcSetModeSelected(true);

                            Building[] lst = new Building[4];
                            lst[0] = Vars.world.build(t.x, t.y - 1);
                            lst[1] = Vars.world.build(t.x, t.y + 1);
                            lst[2] = Vars.world.build(t.x - 1, t.y);
                            lst[3] = Vars.world.build(t.x + 1, t.y);

                            for (Building s : lst) if (s != null) if (s.block == b.block && s.team == b.team) toAdd.add(s);
                        }
                        selected.add(stack);
                        stack = toAdd;
                    }
                }

                if (selectedNumDelta == 0) frag.forceSelectOpt = !frag.forceSelectOpt;
                if (selectedNumDelta != 0) frag.forceSelectOpt = false;

                frag.rebuild();
            }

            if (!isOn) {
                for (Building b : selected) ((IFcBuilding) b).fcSetModeSelected(false);
                selected.clear();
                selectedNumDelta = 0;
                selecting = false;
            }
        });

        fFcDesktopInput.addDrawTopHandle(() -> {
            if (!isOn()) return;

            for (Building b : selected) {
                Tile t = b.tile();
                int size = b.block.size;
                if (!b.block.isMultiblock()) size = 1;
                float offset = size % 2 == 0 ? Vars.tilesize : Vars.tilesize / 2f;

                float cx = t.x * Vars.tilesize + offset;
                float cy = t.y * Vars.tilesize + offset;
                float s = size * Vars.tilesize;

                float ta = s / 2f;
                float tb = s / 6f;

                Drawf.circles(cx, cy, 1.44f, Pal.accent);

                Draw.color(Pal.accent);
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
            }

            if (selecting) {
                Draw.color(deselect ? deselectRectColor : selectRectColor);
                Fill.crect(x, y, w, h);
                Draw.color();
            }
        });

        Vars.ui.hudGroup.addChild(frag);
    }

    public static boolean isOn() {
        return isOn && enabled;
    }

    public static void addFeature(iFeature feature) {
        if (!frag.categories.contains(feature.category)) frag.categories.add(feature.category);
        if (!frag.features.contains(feature)) frag.features.add(feature);
    }
}
