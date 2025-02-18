package finalCampaign.feature.auxDisplay;

import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.pooling.*;
import finalCampaign.*;
import mindustry.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;

public class playerBuildPlans {

    public final Seq<BuildPlan> buildPlans;
    public final IntMap<tileBuildPlan> plans;
    private Pool<tileBuildPlan> planPool;
    private Rect rect = new Rect();
    private Team lastTeam;

    public playerBuildPlans() {
        buildPlans = new Seq<>();
        plans = new IntMap<>();
        planPool = new Pool<>() {
            @Override
            protected tileBuildPlan newObject() {
                return new tileBuildPlan();
            }
        };
    }

    public tileBuildPlan planAtWorldPos(float x, float y) {
        int tx = (int) (x / Vars.tilesize);
        int ty = (int) (y / Vars.tilesize);

        return planAtTilePos(tx, ty);
    }

    public tileBuildPlan planAtTilePos(int x, int y) {
        for (var plan : plans.values()) {
            for (var p : plan.values()) {

                Block block = p.block;
                if (block == null) {
                    Building build = Vars.world.build(p.x, p.y);
                    if (build == null)
                        continue;
                    block = build.block;
                }

                int offset = block.size / 2 - (block.size + 1) % 2;
                rect.set(p.x - offset, p.y - offset, block.size, block.size);
                if (rect.contains(x, y))
                    return plan;
            }
        }

        return null;
    }

    public void addPlan(Player player, BuildPlan plan) {
        int pos = Vars.world.packArray(plan.x, plan.y);
        var tilePlan = plans.get(pos);

        if (tilePlan == null)
            plans.put(pos, tilePlan = planPool.obtain());

        tilePlan.put(player, plan);
        if (Vars.player.team() != lastTeam)
            refreshBuildPlans();
        else
            buildPlans.add(plan);
    }

    public void removePlan(Player player, int x, int y) {
        var tilePlan = planAtTilePos(x, y);

        if (Vars.player.team() != lastTeam)
            refreshBuildPlans();
        else
            buildPlans.remove(tilePlan.get(player));
        tilePlan.remove(player);

        if (tilePlan.isEmpty()) {
            planPool.free(tilePlan);
            plans.remove(Vars.world.packArray(x, y));
        }
    }

    public void clear() {
        for (var p : plans.values()) {
            p.clear();
            planPool.free(p);
        }
        buildPlans.clear();
        plans.clear();
        lastTeam = null;
    }

    private void refreshBuildPlans() {
        buildPlans.clear();

        for (var plan : plans.values()) {
            for (var pair : plan) {
                if (pair.key.team() == Vars.player.team())
                    buildPlans.add(pair.value);
            }
        }

        lastTeam = Vars.player.team();
    }

    public static class tileBuildPlan extends ObjectMap<Player, BuildPlan> implements Pool.Poolable, Displayable {

        @Override
        public void reset() {
            clear();
        }

        @Override
        public void display(Table table) {
            table.table(t -> {
                t.left();
                t.image(Icon.hammer).size(32f);
                t.add(bundle.get("auxDisplay.playerBuildPlans.title"));
            }).growX().left().row();
            table.table(t -> {
                for (var pair : this) {
                    if (pair.key.team() != Vars.player.team())
                        continue;

                    Building build = Vars.world.build(pair.value.x, pair.value.y);
                    Block block = pair.value.block == null ? build == null ? null : build.block : pair.value.block;

                    if (block == null)
                        continue;

                    t.add(pair.key.coloredName()).row();
                    t.image(block.uiIcon).size(16f).padRight(2f);
                    t.add(block.localizedName).row();
                }
            }).growX().row();

            if (!values().hasNext)
                return;

            BuildPlan plan = values().next();
            Building build = Vars.world.build(plan.x, plan.y);
            if (build != null) {
                table.image().height(4f).growX().color(Pal.gray).margin(4f).row();
                build.display(table);
            }
        }
    }
}
