package finalCampaign.patch.annotation;

import java.lang.annotation.*;

/** Records what packages need to import while patching. */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PatchImport {
    String[] value();
}
