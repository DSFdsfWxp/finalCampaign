package finalCampaign.patch.annotation;

import java.lang.annotation.*;

/** It indicates that a field, method, constructor or sub class in a patch class needs to replace it's target class's member that has the same name and parameter type. */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PatchReplace {
    
}
