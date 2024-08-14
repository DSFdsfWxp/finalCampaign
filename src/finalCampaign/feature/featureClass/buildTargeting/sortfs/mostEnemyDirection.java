package finalCampaign.feature.featureClass.buildTargeting.sortfs;

import arc.math.*;
import finalCampaign.feature.featureClass.buildTargeting.fcSortf.*;
import finalCampaign.feature.featureClass.buildTargetingLimit.*;
import finalCampaign.patch.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class mostEnemyDirection extends baseSortf<NoneConfig> {
    protected treeNode root;
    protected int depth;
    private boolean needReset;
    private fcFilter filter;

    public mostEnemyDirection(TurretBuild build) {
        super("mostEnemyDirection", build);
        filter = ((IFcTurretBuild) build).fcFilter();
        root = new treeNode(360);
        calcDepth();
        needReset = false;
    }

    @Override
    public void beforeTargeting() {
        needReset = true;
    }

    public NoneConfig defaultConfig() {
        return new NoneConfig();
    }

    public boolean isValid() {
        return true;
    }

    public float calc(Unit unit) {
        checkReset(true);
        return clampFloat(readFromTree(build.angleTo(unit)));
    }

    public float calc(Building building) {
        checkReset(false);
        return clampFloat(readFromTree(build.angleTo(building)));
    }

    private void calcDepth() {
        double t = 360 / (2 * Math.PI * build.range());
        for (depth=0; ; depth++) if (t * Math.pow(10, depth) >= 1d) break;
    }

    private void addToTree(float from, float to) {
        int gd = 1;
        for (int d=-depth; d<=0 && d>=-depth; d+=gd) {
            if (d==0) gd = -1;
            float nd = (float) Math.pow(10, d);
            int c = 10 - (int) (from * Math.pow(10, -d)) % 10;

            for (int i=0; i<c; i++) {
                if (from >= to) return;
                
                treeNode node = root;
                for (int p=0; p<=-d; p++) {
                    int n = p == 0 ? (int) from : (int) ((from % Math.pow(10, -p + 1)) * Math.pow(10, p));
                    if (node.children[n] == null) node.children[n] = new treeNode(10);
                    node = node.children[n];
                    node.count ++;
                }

                from += nd;
            }
        }
    }

    private void addToTree(float x, float y, float hitSize) {
        float r = hitSize / 2f;

        float x1 = x - r - build.x;
        float x2 = x + r - build.x;
        float y1 = y - r - build.y;
        float y2 = y + r - build.y;

        float tx = x1 * x2;
        float ty = y1 * y2;

        float t1 = x1 * y1;
        float t2 = x2 * y2;
        float t3 = x1 * y2;
        float t4 = x2 * y1;

        float p1x = 0f, p1y = 0;
        float p2x = 0f, p2y = 0;
        float from = 0f, to = 0f;

        if (tx < 0f && ty < 0f) {
            root.count ++;
            return;
        } else if (tx <= 0f && ty >= 0f && !(tx == 0f && ty == 0f)) {
            p1x = x1;
            p1y = (y1 > 0f) ? y1 : y2;
            p2x = x2;
            p2y = p1y;
        } else if (ty <= 0f && tx >= 0f && !(tx == 0f && ty == 0f)) {
            p1x = (x1 > 0f) ? x1 : x2;
            p1y = y1;
            p2x = p1x;
            p2y = y2;
        } else if (t1 > 0f || t2 > 0f) {
            p1x = x1;
            p1y = y2;
            p2x = x2;
            p2y = y1;
        } else if (t3 < 0f || t4 < 0f) {
            p1x = x1;
            p1y = y1;
            p2x = x2;
            p2y = y2;
        }

        from = Angles.angle(p1x, p1y, build.x, build.y);
        to = Angles.angle(p2x, p2y, build.x, build.y);
        addToTree(Math.min(from, to), Math.max(from, to));
    }

    private int readFromTree(float angle) {
        int c = root.count;
        treeNode node = root;

        for (int i=0; i<depth; i++) {
            int n = i == 0 ? (int) angle : (int) ((angle % Math.pow(10, -i + 1)) * Math.pow(10, i));
            if (node.children[n] == null) return c;
            node = node.children[n];
            c += node.count;
        }

        return c;
    }

    private void checkReset(boolean targetUnit) {
        if (!needReset) return;

        root.clear();
        float range = build.range();
        if (targetUnit) {
            Units.nearbyEnemies(build.team, build.x - range, build.y - range, range*2f, range*2f, e -> {
                if(e.dead() || e.team == Team.derelict || !e.within(build.x, build.y, range + e.hitSize/2f) || !e.targetable(build.team) || e.inFogTo(build.team)) return;
                if ((e.isGrounded() && !block.targetGround) || (!e.isGrounded() && !block.targetAir) || !(filter.filters.size > 0 ? filter.unitFilter.get(e) : block.unitFilter.get(e))) return;

                addToTree(e.x, e.y, e.hitSize);
            });
        } else {
            Vars.indexer.allBuildings(build.x, build.y, range, e -> {
                if (e.dead() || (e.team == Team.derelict && !Vars.state.rules.coreCapture) || e.team == build.team || !block.targetGround || !(filter.filters.size > 0 ? filter.buildingFilter.get(e) : block.buildingFilter.get(e))) return;

                addToTree(e.x, e.y, e.hitSize());
            });
        }

        needReset = false;
    }

    protected static class treeNode {
        int count;
        treeNode[] children;

        protected treeNode(int childrenNum) {
            children = new treeNode[childrenNum];
            count = 0;
        }

        void clear() {
            count = 0;
            children = new treeNode[children.length];
        }
    }
}
