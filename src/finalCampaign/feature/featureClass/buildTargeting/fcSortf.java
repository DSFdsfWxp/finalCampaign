package finalCampaign.feature.featureClass.buildTargeting;

import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.io.*;
import finalCampaign.*;
import finalCampaign.patch.*;
import finalCampaign.util.*;
import mindustry.entities.Units.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class fcSortf implements Sortf {
    protected final static ObjectMap<String, Func<TurretBuild, baseSortf<?>>> provs = new ObjectMap<>();
    protected final static Seq<String> lst = new Seq<>();
    
    public final Seq<baseSortf<?>> sortfs;
    private final TurretBuild build;
    private final IFcAttractiveEntityType buildType;

    public static void register(String name, Func<TurretBuild, baseSortf<?>> prov) {
        provs.put(name, prov);
        lst.add(name);
    }

    public static String[] sortfLst() {
        return lst.toArray(String.class);
    }

    public fcSortf(TurretBuild build) {
        this.build = build;
        buildType = (IFcAttractiveEntityType) build.block;
        sortfs = new Seq<>();
    }

    public float cost(Unit unit, float x, float y) {
        float score = 0f;
        
        for (int i=0; i<sortfs.size; i++) {
            float s = Mathf.clamp(sortfs.get(i).calc(unit));
            if (s > 0) score += s + Math.pow(2, i + 1) - 1;
        }

        if (!build.isControlled()) score += calcAttractive((IFcAttractiveEntityType) unit.type, new Vec2(unit.x, unit.y));

        return -score;
    }

    public float cost(Building building) {
        float score = 0f;
        
        for (int i=0; i<sortfs.size; i++) {
            float s = Mathf.clamp(sortfs.get(i).calc(building));
            if (s > 0) score += s + Math.pow(2, i + 1) - 1;
        }

        if (!build.isControlled()) score += calcAttractive((IFcAttractiveEntityType) building.block, new Vec2(building.x, building.y));

        return -score;
    }

    public boolean isValid() {
        for (baseSortf<?> sortf : sortfs) if (!sortf.isValid()) return false;
        return true;
    }

    private float calcAttractive(IFcAttractiveEntityType type, Vec2 targetPos) {
        Vec2 pos = new Vec2(build.x, build.y);
        Vec2 currentDirection = build.targetPos.cpy().nor();

        Vec2 deltaPos = pos.cpy().sub(targetPos);
        float dst = deltaPos.len();
        float angleFactor = currentDirection.dot(deltaPos) / (currentDirection.len() * dst) + 2;
        float dstFactor = 1.5f;
        if (dst >= 5f) {
            double x4 = 3d * Math.sqrt(build.range()) + 120d;
            double x2 = 2d / 3d * (x4 + 5d);
            double x3 = 1d / 3d * (x4 + 5d);
            bezier bezier = new bezier(5d, 1.5d, x2, 1.5d, x3, 0d, x4, 0d);
            dstFactor = (float) bezier.solve(dst);
        }
        float val = angleFactor * dstFactor * type.fcAttractiveness() - buildType.fcAntiAttractiveness();
        return val > 0 ? val - type.fcHiddenness() : 0f;
    }

    public void read(Reads reads) {
        int count = reads.i();
        for (int i=0; i<count; i++) {
            baseSortf<?> sortf = provs.get(reads.str()).get(build);
            sortf.read(reads);
            sortfs.add(sortf);
        }
    }

    public void write(Writes writes) {
        writes.i(sortfs.size);
        for (baseSortf<?> sortf : sortfs) {
            writes.str(sortf.name);
            sortf.write(writes);
        }
    }

    public void beforeTargeting() {
        for (baseSortf<?> sortf : sortfs) sortf.beforeTargeting();
    }

    public fcSortf clone(TurretBuild build) {
        fcSortf fcsortf = new fcSortf(build);
        for (baseSortf<?> sortf : sortfs) fcsortf.sortfs.add(sortf.clone(build));
        return fcsortf;
    }

    public static class NoneConfig {}
    
    public static abstract class baseSortf<T> {
        public final String name;
        public final String localizedName;
        public final TurretBuild build;
        public final IFcTurretBuild fcBuild;
        public final Turret block;
        public T config;

        public baseSortf(String name, TurretBuild build) {
            this.name = name;
            this.build = build;
            this.block = (Turret) build.block;
            fcBuild = (IFcTurretBuild) build;
            localizedName = bundle.get("sortf." + name, name);
            this.config = defaultConfig();
        }

        protected float clampFloat(float v) {
            if (v < 1) v = 1f;
            int s = (int) Math.log10(v);
            return s / 100f + v / (float) Math.pow(10, s + 3);
        }

        public boolean hasConfig() {
            return !defaultConfig().getClass().equals(NoneConfig.class);
        }

        public Class<?> configType() {
            return defaultConfig().getClass();
        }

        @SuppressWarnings("unchecked")
        public baseSortf<T> clone(TurretBuild build) {
            baseSortf<T> out = (baseSortf<T>) fcSortf.provs.get(name).get(build);
            out.config = config;
            return out;
        }

        public void write(Writes writes) {}
        public void read(Reads reads) {}
        public void beforeTargeting() {}

        public abstract T defaultConfig();
        public abstract boolean isValid();
        public abstract float calc(Building building);
        public abstract float calc(Unit unit);

    }
}
