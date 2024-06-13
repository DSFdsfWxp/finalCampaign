package finalCampaign.patch.annotation;

import java.lang.annotation.*;

/** It indicates that we need to change the modifier of a field or method so that we can access it in the sub class. */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PatchAccess {
    
}
