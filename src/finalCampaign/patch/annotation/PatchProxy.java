package finalCampaign.patch.annotation;

import java.lang.annotation.*;

/** Marks that a class is used to make a proxy patch */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PatchProxy {
    
}
