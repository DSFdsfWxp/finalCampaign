package finalCampaign.patch.annotation;

import java.lang.annotation.*;

/** Marks that a class is used to make a modification patch */
@Documented
@SuppressWarnings("rawtypes")
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PatchModify {
    Class value();
}
