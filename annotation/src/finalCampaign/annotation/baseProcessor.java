package finalCampaign.annotation;

import javax.annotation.processing.*;
import javax.lang.model.util.*;

public abstract class baseProcessor extends AbstractProcessor {

    protected Filer filer;
    protected Elements elements;
    protected Types types;
    protected Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        elements = processingEnv.getElementUtils();
        types = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
    }
}
