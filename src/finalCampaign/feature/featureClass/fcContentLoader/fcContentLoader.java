package finalCampaign.feature.featureClass.fcContentLoader;

import java.lang.reflect.*;
import arc.util.*;
import mindustry.core.*;
import mindustry.ctype.*;

public class fcContentLoader extends ContentLoader {
    public fcContentLoader(ContentLoader originalLoader) {
        for (Field field : ContentLoader.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                field.set(this, field.get(originalLoader));
            } catch(Exception e) {
                Log.err(e);
                throw new RuntimeException("failed to init fcContentLoader.");
            }
        }
    }

    @Override
    public void handleContent(Content content) {
        super.handleContent(content);
    }

    @Override
    public void handleMappableContent(MappableContent content) {
        super.handleMappableContent(content);
    }
}
