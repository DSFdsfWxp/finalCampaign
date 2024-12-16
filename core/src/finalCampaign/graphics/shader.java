package finalCampaign.graphics;

import arc.files.*;
import arc.graphics.gl.*;
import finalCampaign.*;
import mindustry.graphics.*;

public class shader extends Shader {

    public shader(String vertexShaderName, String fragmentShaderName) {
        super(getShaderFi(vertexShaderName + ".vert"), getShaderFi(fragmentShaderName + ".frag"));
    }

    public static Fi getShaderFi(String name) {
        if (name.startsWith("raw.")) return Shaders.getShaderFi(name.substring(4));
        return finalCampaign.thisModFi.child("fcShader").child(name);
    }
    
}
