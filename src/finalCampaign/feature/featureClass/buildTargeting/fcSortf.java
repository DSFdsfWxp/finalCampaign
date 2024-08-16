package finalCampaign.feature.featureClass.buildTargeting;

import java.io.*;
import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import finalCampaign.*;
import finalCampaign.patch.*;
import finalCampaign.util.*;
import mindustry.*;
import mindustry.entities.Units.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class fcSortf implements Sortf {
    protected final static ObjectMap<String, Func<TurretBuild, baseSortf<?>>> provs = new ObjectMap<>();
    protected final static Seq<String> lst = new Seq<>();
    
    public final Seq<baseSortf<?>> buildSortfs;
    public final Seq<baseSortf<?>> unitSortfs;
    public final TurretBuild build;
    private final IFcAttractiveEntityType buildType;
    private timer debugTimer = new timer();
    private boolean print = false;

    public static void register(String name, Func<TurretBuild, baseSortf<?>> prov) {
        provs.put(name, prov);
        lst.add(name);
    }

    public static String[] sortfLst() {
        return lst.toArray(String.class);
    }

    public static String localizedName(String name) {
        return bundle.get("sortf." + name, name);
    }

    public fcSortf(TurretBuild build) {
        this.build = build;
        buildType = (IFcAttractiveEntityType) build.block;
        unitSortfs = new Seq<>();
        buildSortfs = new Seq<>();
        debugTimer.mark();
    }

    public float cost(Unit unit, float x, float y) {
        float score = 0f;
        
        for (int i=0; i<unitSortfs.size; i++) {
            float s = Mathf.clamp(unitSortfs.get(i).calc(unit));
            if (s > 0) score += s + Math.pow(2, unitSortfs.size - 1) - 1;
        }

        if (print) {
            Log.debug(unit.type.localizedName + " " + Float.toString(-score));
        }

        if (!build.isControlled()) score += calcAttractive((IFcAttractiveEntityType) unit.type, new Vec2(unit.x, unit.y));

        if (print) {
            Log.debug(unit.type.localizedName + " f " + Float.toString(-score));
        }

        return -score;
    }

    public float cost(Building building) {
        float score = 0f;
        
        for (int i=0; i<buildSortfs.size; i++) {
            float s = Mathf.clamp(buildSortfs.get(i).calc(building));
            if (s > 0) score += s * Math.pow(2, buildSortfs.size - i);
        }

        if (print) {
            Log.debug(building.block.localizedName + " " + Float.toString(-score));
        }

        if (!build.isControlled()) score += calcAttractive((IFcAttractiveEntityType) building.block, new Vec2(building.x, building.y));

        if (print) {
            Log.debug(building.block.localizedName + " f " + Float.toString(-score));
        }

        return -score;
    }

    public boolean isValid() {
        for (baseSortf<?> sortf : unitSortfs) if (!sortf.isValid()) return false;
        for (baseSortf<?> sortf : buildSortfs) if (!sortf.isValid()) return false;
        return true;
    }

    @Nullable
    public baseSortf<?> get(String name, boolean unit) {
        int pos = indexOf(name, unit);
        if (pos == -1) return null;
        if (unit) return unitSortfs.get(pos);
        return buildSortfs.get(pos);
    }

    public boolean has(String name, boolean unit) {
        return indexOf(name, unit) != -1;
    }

    public int indexOf(String name, boolean unit) {
        if (unit) {
            for (int i=0; i<unitSortfs.size; i++) if (unitSortfs.get(i).name.equals(name)) return i;
        } else {
            for (int i=0; i<buildSortfs.size; i++) if (buildSortfs.get(i).name.equals(name)) return i;
        }
        return -1;
    }

    public void add(String name, boolean unit) {
        if (has(name, unit)) return;
        var prov = provs.get(name);
        if (prov == null) return;

        if (unit) {
            unitSortfs.add(prov.get(build));
            unitSortfs.peek().unitSide = true;
        } else {
            buildSortfs.add(prov.get(build));
            buildSortfs.peek().unitSide = false;
        }
    }

    public void remove(String name, boolean unit) {
        Seq<baseSortf<?>> sortfs = unit ? unitSortfs : buildSortfs;
        Seq<baseSortf<?>> toRemove = new Seq<>();
        for (baseSortf<?> sortf : sortfs) if (sortf.name.equals(name)) toRemove.add(sortf);
        sortfs.removeAll(toRemove);
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
        return val > 0 ? Math.max(0f, val - type.fcHiddenness()) : 0f;
    }

    public void read(Reads reads) {
        int uc = reads.i();
        int bc = reads.i();
        Seq<baseSortf<?>> uRead = new Seq<>();
        Seq<baseSortf<?>> bRead = new Seq<>();

        for (int i=0; i<uc; i++) {
            String name = reads.str();

            int pos = indexOf(name, true);
            baseSortf<?> current = pos == -1 ? current = provs.get(name).get(build) : unitSortfs.get(pos);

            current.unitSide = true;
            current.read(reads);
            uRead.add(current);
        }
        for (int i=0; i<bc; i++) {
            String name = reads.str();

            int pos = indexOf(name, false);
            baseSortf<?> current = pos == -1 ? current = provs.get(name).get(build) : buildSortfs.get(pos);

            current.unitSide = false;
            current.read(reads);
            bRead.add(current);
        }

        unitSortfs.clear();
        buildSortfs.clear();
        unitSortfs.addAll(uRead);
        buildSortfs.addAll(bRead);
    }

    public void read(byte[] data) {
        Reads r = new Reads(new DataInputStream(new ByteArrayInputStream(data)));
        read(r);
        r.close();
    }

    public void write(Writes writes) {
        writes.i(unitSortfs.size);
        writes.i(buildSortfs.size);

        for (baseSortf<?> sortf : unitSortfs) {
            writes.str(sortf.name);
            sortf.write(writes);
        }
        for (baseSortf<?> sortf : buildSortfs) {
            writes.str(sortf.name);
            sortf.write(writes);
        }
    }

    public byte[] write() {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        Writes w = new Writes(new DataOutputStream(s));
        write(w);
        w.close();
        return s.toByteArray();
    }

    public void beforeTargeting() {
        for (baseSortf<?> sortf : unitSortfs) sortf.beforeTargeting();
        for (baseSortf<?> sortf : buildSortfs) sortf.beforeTargeting();
        print = false;
        if (debugTimer.sTime() > 10f && (Vars.net.server() || !Vars.net.active())) {
            print = true;
            debugTimer.mark();
        }
    }

    public fcSortf clone(TurretBuild build) {
        fcSortf fcsortf = new fcSortf(build);
        for (baseSortf<?> sortf : unitSortfs) fcsortf.unitSortfs.add(sortf.clone(build));
        for (baseSortf<?> sortf : buildSortfs) fcsortf.buildSortfs.add(sortf.clone(build));
        return fcsortf;
    }

    public static class NoneConfig {}
    
    public static abstract class baseSortf<T> {
        public final String name;
        public final String localizedName;
        public final TurretBuild build;
        public final IFcTurretBuild fcBuild;
        public final Turret block;
        public boolean unitSide;
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
            out.unitSide = unitSide;
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
