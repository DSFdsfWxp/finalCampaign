package finalCampaign.feature.featureClass.binding;

import arc.KeyBinds.*;
import arc.input.InputDevice.*;
import arc.input.KeyCode;

public enum binding implements KeyBind {

    freeVision(KeyCode.f4, "finalCampaign"),
    roulette(KeyCode.tab)
    ;
    
    private final KeybindValue defaultValue;
    private final String category;

    binding(KeybindValue defaultValue, String category){
        this.defaultValue = defaultValue;
        this.category = category;
    }

    binding(KeybindValue defaultValue){
        this(defaultValue, null);
    }

    @Override
    public KeybindValue defaultValue(DeviceType type){
        return defaultValue;
    }

    @Override
    public String category(){
        return category;
    }
}
