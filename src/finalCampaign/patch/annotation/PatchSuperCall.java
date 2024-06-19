package finalCampaign.patch.annotation;

import java.lang.annotation.*;

/** 
 * It indicates that a method in a patch class needs to be add it's target class, which will call a super method
 * with the same name, arg list and return type 
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PatchSuperCall {
    
}
