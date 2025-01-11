package finalCampaign.annotation.util;

import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;

public class annotationsUtil {
    private HashMap<TypeMirror, AnnotationMirror> map;

    public annotationsUtil(List<? extends AnnotationMirror> lst) {
        map = new HashMap<>();

        for (AnnotationMirror am : lst)
            map.put(am.getAnnotationType().asElement().asType(), am);
    }

    public annotationUtil getAnnotation(String className) {
        for (var e : map.entrySet()) {
            if (e.getKey().toString().equals(className))
                return new annotationUtil(e.getValue());
        }
        return null;
    }

    public annotationUtil getAnnotation(TypeMirror type) {
        for (var e : map.entrySet()) {
            if (e.getKey().equals(type))
                return new annotationUtil(e.getValue());
        }
        return null;
    }

    
    public static class annotationUtil {
        private AnnotationMirror am;
        private HashMap<String, Object> valueMap;

        public annotationUtil(AnnotationMirror am) {
            this.am = am;
            valueMap = new HashMap<>();

            for (var e : am.getElementValues().entrySet())
                valueMap.put(e.getKey().getSimpleName().toString(), e.getValue().getValue());
        }

        @SuppressWarnings("unchecked")
        public <T> T getValue(String name) {
            return (T) valueMap.get(name);
        }

        public TypeMirror getType() {
            return am.getAnnotationType().asElement().asType();
        }
    }
}
