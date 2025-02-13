package finalCampaign.graphics;

import arc.files.*;
import arc.graphics.gl.*;
import finalCampaign.*;
import mindustry.graphics.*;

public class fcShader extends Shader {

    public fcShader(String vertexShaderName, String fragmentShaderName) {
        super(getShaderFi(vertexShaderName + ".vert"), getShaderFi(fragmentShaderName + ".frag"));
    }

    public static Fi getShaderFi(String name) {
        if (name.startsWith("raw.")) return Shaders.getShaderFi(name.substring(4));
        return finalCampaign.thisModZip.child("fcShader").child(name);
    }
    
}
