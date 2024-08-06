package finalCampaign.util;

import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import finalCampaign.*;
import finalCampaign.patch.*;
import mindustry.entities.Units.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import mindustry.world.blocks.storage.CoreBlock.*;

public class fcSortf implements Sortf {
    private Entityc callerEntity;
    private Posc callerPosc;
    private Ranged callerRanged;
    private IFcAttractiveEntityType callerAttractiveEntityType;
    private Prov<Vec2> callerCurrentDirection;
    private Prov<Boolean> callerControlled;
    public Seq<fcSortfs> sortfs;

    public fcSortf(Entityc caller) {
        sortfs = new Seq<>();

        callerEntity = caller;
        callerPosc = (Posc) caller;
        callerRanged = (Ranged) caller;
        if (caller instanceof Unit unit) {
            callerAttractiveEntityType = (IFcAttractiveEntityType) unit.type;
            callerCurrentDirection = () -> unit.vel().cpy().nor();
            callerControlled = () -> unit.isPlayer();
        } else if (caller instanceof TurretBuild building) {
            callerAttractiveEntityType = (IFcAttractiveEntityType) building.block;
            callerCurrentDirection = () -> building.targetPos.cpy().nor();
            callerControlled = () -> building.isControlled();
        }
    }

    public float cost(Unit unit, float x, float y) {
        return cost(unit);
    }

    public float cost(Entityc entity) {
        float score = 0f;
        for (int i=0; i<sortfs.size; i++) {
            float s = Mathf.clamp(sortfs.get(i).calc(callerEntity, entity));
            if (s > 0) score += s + Math.pow(2, i + 1) - 1;
        }

        if (!callerControlled.get()) {
            Vec2 pos = new Vec2(callerPosc.getX(), callerPosc.getY());
            Vec2 targetPos = new Vec2();
            Vec2 currentDirection = callerCurrentDirection.get();
            IFcAttractiveEntityType attractiveSource = null;
    
            if (entity instanceof Unit unit) {
                targetPos.set(unit.x, unit.y);
                attractiveSource = (IFcAttractiveEntityType) unit.type;
            } else if (entity instanceof Building building) {
                targetPos.set(building.x, building.y);
                attractiveSource = (IFcAttractiveEntityType) building.block;
            }
    
            Vec2 deltaPos = pos.cpy().sub(targetPos);
            float dst = deltaPos.len();
            float angleFactor = currentDirection.dot(deltaPos) / (currentDirection.len() * dst) + 2;
            float dstFactor = 1.5f;
            if (dst >= 5f) {
                double x4 = 3d * Math.sqrt(callerRanged.range()) + 120d;
                double x2 = 2d / 3d * (x4 + 5d);
                double x3 = 1d / 3d * (x4 + 5d);
                bezier bezier = new bezier(5d, 1.5d, x2, 1.5d, x3, 0d, x4, 0d);
                dstFactor = (float) bezier.solve(dst);
            }
            float val = angleFactor * dstFactor * attractiveSource.fcAttractiveness() - callerAttractiveEntityType.fcAntiAttractiveness();
            if (val > 0) score += val - attractiveSource.fcHiddenness();
        }

        return -score;
    }

    private static float clampFloat(float v) {
        int s = (int) Math.log10(v);
        return s / 100f + v / (float) Math.pow(10, s + 3);
    }

    public static enum fcSortfs {
        hightestMaxHealth((t, e) -> {
            Healthc healthc = (Healthc) e;
            return clampFloat(healthc.maxHealth());
        }),
        hightestHealth((t, e) -> {
            Healthc healthc = (Healthc) e;
            return clampFloat(healthc.health());
        }),
        lowHealth((t, e) -> {
            Healthc healthc = (Healthc) e;
            return healthc.healthf() > 0.1f ? 0f : 1f;
        }),
        nearestToCore((t, e) -> {
            Teamc teamcT = (Teamc) t;
            Posc poscT = (Posc) t;
            Teamc teamcE = (Teamc) e;
            Vec2 pos = null;

            if (teamcT.team() == teamcE.team()) {
                CoreBuild b = teamcE.closestCore();
                pos = new Vec2(b.x, b.y);
            } else {
                CoreBuild b = teamcE.closestEnemyCore();
                pos = new Vec2(b.x, b.y);
            }

            return 1f - clampFloat(pos.sub(poscT.getX(), poscT.getY()).len());
        }),
        nearest((t, e) -> {
            Posc poscT = (Posc) t;
            Posc poscE = (Posc) e;
            return clampFloat((new Vec2(poscT.getX() - poscE.getX(), poscT.getY() - poscE.getY())).len());
        }),
        farest((t, e) -> {
            Posc poscT = (Posc) t;
            Posc poscE = (Posc) e;
            return 1f - clampFloat((new Vec2(poscT.getX() - poscE.getX(), poscT.getY() - poscE.getY())).len());
        }),
        uneffected((t, e) -> {
            if (e instanceof Unit eu) {
                if (t instanceof TurretBuild tb) {
                    return eu.hasEffect(tb.peekAmmo().status) ? 0f : 1f;
                }
                return 0f;
            }
            return 0f;
        }),
        effected((t, e) -> {
            if (e instanceof Unit u) {
                return u.statusBits().isEmpty() ? 0f : 1f;
            }
            return 0f;
        })
        ;

        private Func2<Entityc, Entityc, Float> func;

        fcSortfs(Func2<Entityc, Entityc, Float> func) {
            this.func = func;
        }

        public float calc(Entityc build, Entityc unit) {
            return func.get(build, unit);
        }

        public String localizedName() {
            return bundle.get("sortf." + name(), name());
        }
    }
}
