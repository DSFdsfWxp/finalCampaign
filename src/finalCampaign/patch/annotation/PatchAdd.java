package finalCampaign.patch.annotation;

import java.lang.annotation.*;

/** It indicates that a field, method, constructor or sub class in a patch class needs to be add it's target class */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PatchAdd {
    
}
