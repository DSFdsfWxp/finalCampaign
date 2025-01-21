package finalCampaign.launch;

import arc.struct.*;
import org.spongepowered.asm.service.*;

public class shareGlobalPropertyService implements IGlobalPropertyService {
    ObjectMap<String, Object> map;

    public shareGlobalPropertyService() {
        map = new ObjectMap<>();
    }

    public IPropertyKey resolveKey(String name) {
        return new Key(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(IPropertyKey key, T defaultValue) {
        return (T) map.get(key.toString(), defaultValue);
    }

    public <T> T getProperty(IPropertyKey key) {
        return getProperty(key, null);
    }

    public String getPropertyString(IPropertyKey key, String defaultValue) {
        return getProperty(key, defaultValue);
    }

    public void setProperty(IPropertyKey key, Object value) {
        map.put(key.toString(), value);
    }

    static class Key implements IPropertyKey {
        private final String key;
        
        Key(String key) {
          this.key = key;
        }
        
        public String toString() {
          return this.key;
        }
    }
}
