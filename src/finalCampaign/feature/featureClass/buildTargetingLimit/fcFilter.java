package finalCampaign.feature.featureClass.buildTargetingLimit;

import arc.func.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import finalCampaign.*;
import finalCampaign.patch.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class fcFilter {
    protected static final ObjectMap<String, Func<TurretBuild, baseFilter<?>>> provs = new ObjectMap<>();
    protected static final Seq<String> lst = new Seq<>();

    public final Boolf<Unit> unitFilter;
    public final Boolf<Building> buildingFilter;
    public final Seq<baseFilter<?>> filters;
    public final TurretBuild build;

    public static void register(String name, Func<TurretBuild, baseFilter<?>> prov) {
        provs.put(name, prov);
        lst.add(name);
    }

    public static String[] filterLst() {
        return lst.toArray(String.class);
    }

    public static String localizedName(String name) {
        return bundle.get("filter." + name, name);
    }

    public fcFilter(TurretBuild build) {
        this.build = build;
        filters = new Seq<>();

        unitFilter = u -> {
            for (baseFilter<?> filter : filters) if (!filter.get(u)) return false;
            return true;
        };

        buildingFilter = b -> {
            for (baseFilter<?> filter : filters) if (!filter.get(b)) return false;
            return true;
        };
    }

    public boolean has(String name) {
        for (baseFilter<?> filter : filters) if (filter.name.equals(name)) return true;
        return false;
    }

    @Nullable
    public baseFilter<?> get(String name) {
        for (baseFilter<?> filter : filters) if (filter.name.equals(name)) return filter;
        return null;
    }

    public void add(String name) {
        filters.add(provs.get(name).get(build));
    }

    public void read(Reads reads) {
        int c = reads.i();
        Seq<String> readed = new Seq<>();

        for (int i=0; i<c; i++) {
            String name = reads.str();
            if (!has(name)) add(name);
            baseFilter<?> filter = get(name);
            filter.read(reads);
            readed.add(name);
        }

        Seq<baseFilter<?>> toRemove = new Seq<>();
        for (baseFilter<?> filter : filters) if (!readed.contains(filter.name)) toRemove.add(filter);
        filters.removeAll(toRemove);
    }

    public void write(Writes writes) {
        writes.i(filters.size);
        for (baseFilter<?> filter : filters) {
            writes.str(filter.name);
            filter.write(writes);
        }
    }

    public void beforeTargeting() {
        for (baseFilter<?> filter : filters) filter.beforeTargeting();
    }

    public static class NoneConfig {}

    public abstract static class baseFilter<T> {
        public final String name;
        public final String localizedName;
        public final TurretBuild build;
        public final IFcTurretBuild fcBuild;
        public final Turret block;
        public T config;

        public baseFilter(String name, TurretBuild build) {
            this.name = name;
            this.build = build;
            block = (Turret) build.block;
            fcBuild = (IFcTurretBuild) build;
            localizedName = bundle.get("filter." + name, name);
            config = defaultConfig();
        }

        public boolean hasConfig() {
            return !defaultConfig().getClass().equals(NoneConfig.class);
        }

        public Class<?> configType() {
            return defaultConfig().getClass();
        }

        @SuppressWarnings("unchecked")
        public baseFilter<T> clone(TurretBuild build) {
            return (baseFilter<T>) provs.get(name).get(build);
        }

        public void beforeTargeting() {}
        public void read(Reads reads) {}
        public void write(Writes writes) {}

        public abstract T defaultConfig();
        public abstract boolean get(Unit unit);
        public abstract boolean get(Building building);
    }
}
