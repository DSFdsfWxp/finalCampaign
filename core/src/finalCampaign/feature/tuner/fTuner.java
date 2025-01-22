package finalCampaign.feature.tuner;

import java.lang.annotation.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import arc.*;
import arc.func.*;
import finalCampaign.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.gen.*;

public class fTuner {
    private static ObjectMap<String, Boolean> map;
    private static ObjectMap<String, Object> configMap;
    private static tunerPane pane;

    public static boolean supported() {
        return !Vars.headless;
    }

    public static void init() {
        map = new ObjectMap<>();
        configMap = new ObjectMap<>();
        Vars.ui.settings.addCategory(bundle.get("tuner.pane.title"), Icon.hammer, t -> {
            pane = new tunerPane(t);
        });
    }

    public static void load() {

    }

    public static boolean add(String name, boolean def, @Nullable Cons<Boolean> stateCons) {
        if (map.containsKey(name)) throw new RuntimeException("Two tuner items with the same name are not allowed: " + name);
        map.put(name, setting.getAndCast(name, def));
        if (stateCons != null) Events.on(stateChangeEvent.class, e -> {
            if (e.name.equals(name)) stateCons.get(e.value);
        });
        return pane.addItem(name, false, false, null);
    }

    public static boolean add(String name, boolean def, Object config, @Nullable Cons<Boolean> stateCons) {
        if (map.containsKey(name)) throw new RuntimeException("Two tuner items with the same name are not allowed: " + name);
        map.put(name, setting.getAndCast(name, def));
        configMap.put(name, config);
        if (stateCons != null) Events.on(stateChangeEvent.class, e -> {
            if (e.name.equals(name)) stateCons.get(e.value);
        });
        return pane.addItem(name, true, false, config);
    }

    public static void add(String name, Object config) {
        if (map.containsKey(name)) throw new RuntimeException("Two tuner items with the same name are not allowed: " + name);
        map.put(name, false);
        configMap.put(name, config);
        pane.addItem(name, true, true, config);
    }

    public static boolean isOn(String name) {
        if (!map.containsKey(name)) return false;
        return map.get(name);
    }

    public static void set(String name, boolean value) {
        if (!map.containsKey(name)) throw new RuntimeException("The tuner item not exsited: " + name);
        map.put(name, value);
        setting.put("tuner." + name, value);
        Events.fire(new stateChangeEvent(name, value));
    }

    protected static void load(String name) {
        boolean v = (Boolean) setting.get("tuner." + name, map.get(name));
        map.put(name, v);
    }

    public static class floatSlider {
        public float value;
        public float min;
        public float max;
        public float step;

        public floatSlider() {}

        public floatSlider(float def, float max, float min, float step) {
            this.value = def;
            this.max = max;
            this.min = min;
            this.step = step;
        }
    }

    @setable
    public static class stringField implements JsonSerializable {
        public String value;
        public String hint;
        public int maxLength;
        public @Nullable Func<String, String> processor;

        public stringField() {}

        public stringField(String def, String hint, int maxLength, @Nullable Func<String, String> processor) {
            this.value = def;
            this.hint = hint;
            this.maxLength = maxLength;
            this.processor = processor;
        }

        public void read(Json json, JsonValue jsonData) {
            value = jsonData.getString("value", "");
            hint = jsonData.getString("hint", "");
            maxLength = jsonData.getInt("maxLength", 2048);
            processor = null;
        }

        public void write(Json json) {
            json.writeValue("value", value);
            json.writeValue("hint", hint);
            json.writeValue("maxLength", maxLength);
        }

        public void set(stringField src) {
            this.value = src.value;
            this.hint = src.hint;
            this.maxLength = src.maxLength;
        }
    }

    @setable
    public static class uiPosition implements JsonSerializable {
        public float x, y;
        public Prov<Float> originalX, originalY;
        public boolean relatively;

        public uiPosition() {}

        public uiPosition(Prov<Float> originalX, Prov<Float> originalY, boolean relatively) {
            this.originalX = originalX;
            this.originalY = originalY;
            this.relatively = relatively;

            if (relatively) {
                x = 0;
                y = 0;
            } else {
                x = originalX.get();
                y = originalY.get();
            }
        }

        public uiPosition(uiPosition src) {
            set(src);
        }

        public float getX() {
            if (relatively) return x + originalX.get();
            return x;
        }

        public float getY() {
            if (relatively) return y + originalY.get();
            return y;
        }

        public void setAbsolute(float x, float y) {
            if (relatively) {
                this.x = x - originalX.get();
                this.y = y - originalY.get();
            } else {
                this.x = x;
                this.y = y;
            }
        }

        public void setRelatively(boolean relatively) {
            if (relatively == this.relatively) return;
            if (this.relatively) {
                x += originalX.get();
                y += originalY.get();
            } else {
                x -= originalX.get();
                y -= originalY.get();
            }
            this.relatively = relatively;
        }

        public void set(uiPosition src) {
            this.x = src.x;
            this.y = src.y;
            this.relatively = src.relatively;
        }

        public void write(Json json) {
            json.writeValue("x", x);
            json.writeValue("y", y);
            json.writeValue("relatively", relatively);
        }

        public void read(Json json, JsonValue jsonData) {
            x = jsonData.getFloat("x", 0f);
            y = jsonData.getFloat("y", 0f);
            relatively = jsonData.getBoolean("relatively", false);
            originalX = originalY = null;
        }
    }

    @setable
    public static class contentChooser implements JsonSerializable {
        public final int minCountChoosed, maxCountChoosed;
        public final Func<UnlockableContent, Boolean> contentFilter;
        public Seq<UnlockableContent> choosedContents;

        public contentChooser(int minCountChoosed, int maxCountChoosed, Func<UnlockableContent, Boolean> contentFilter) {
            this.maxCountChoosed = maxCountChoosed;
            this.minCountChoosed = minCountChoosed;
            this.contentFilter = contentFilter;
            choosedContents = new Seq<>();
        }

        public void write(Json json) {
            json.writeArrayStart("content");
            for (UnlockableContent c : choosedContents)
                json.writeValue(c.getContentType().name() + "-" + c.name);
            json.writeArrayEnd();
        }

        public void read(Json json, JsonValue jsonData) {
            String[] contents = jsonData.get("content").asStringArray();
            Seq<String> tmp = new Seq<>();
            for (String c : contents) {
                tmp.add(c.split("-"));
                ContentType t = ContentType.valueOf(tmp.get(0));
                tmp.remove(0);
                UnlockableContent uc = Vars.content.getByName(t, String.join("-", tmp));
                if (uc != null)
                    choosedContents.add(uc);
                tmp.clear();
            }
        }
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface setable{
    }

    public static class stateChangeEvent {
        public String name;
        public boolean value;

        public stateChangeEvent(String name, boolean value) {
            this.name = name;
            this.value = value;
        }
    }

}