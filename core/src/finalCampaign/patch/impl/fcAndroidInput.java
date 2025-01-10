package finalCampaign.patch.impl;

import org.spongepowered.asm.mixin.*;
import arc.backend.android.*;
import arc.util.*;

@Mixin(AndroidInput.class)
public abstract class fcAndroidInput {
    @Shadow(remap = false)
    int[] realId;

    public int lookUpPointerIndex(int pointerId){
        int len = realId.length;
        for(int i = 0; i < len; i++){
            if(realId[i] == pointerId) return i;
        }

        Log.err("AndroidInput: Pointer ID lookup failed: " + pointerId);
        return -1;
    }
}
