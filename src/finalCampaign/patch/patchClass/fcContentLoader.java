package finalCampaign.patch.patchClass;

import arc.files.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.mod.Mods.*;
import mindustry.core.*;
import finalCampaign.patch.annotation.*;

@PatchImport({"arc.files", "arc.struct", "arc.util", "mindustry.ctype", "mindustry.mod.Mods", "mindustry.core", "finalCampaign.patch.annotation"})
@PatchModify(ContentLoader.class)
@SuppressWarnings("unchecked")
public class fcContentLoader{
    private ObjectMap<String, MappableContent>[] contentNameMap = new ObjectMap[ContentType.all.length];
    private Seq<Content>[] contentMap = new Seq[ContentType.all.length];
    private @Nullable LoadedMod currentMod;
    private @Nullable Content lastAdded;

    @PatchAdd
    public fcContentLoader(Object originalContentMap){
        contentMap = ((Seq<Content>[])(originalContentMap)).clone();

        for(ContentType type : ContentType.all){
            contentNameMap[type.ordinal()] = new ObjectMap<>();
            
            for (Content content : contentMap[type.ordinal()]){
                if (content instanceof MappableContent mappableContent){

                    contentNameMap[type.ordinal()].put(mappableContent.name, mappableContent);
                }
            }

        }

    }

    @PatchReplace
    public void handleContent(Content content){
        this.lastAdded = content;
        contentMap[content.getContentType().ordinal()].add(content);
    }

    @PatchReplace
    public void handleMappableContent(MappableContent content){
        if(contentNameMap[content.getContentType().ordinal()].containsKey(content.name)){
            throw new IllegalArgumentException("Two content objects cannot have the same name! (issue: '" + content.name + "')");
        }
        if(currentMod != null){
            content.minfo.mod = currentMod;
            if(content.minfo.sourceFile == null){
                content.minfo.sourceFile = new Fi(content.name);
            }
        }
        contentNameMap[content.getContentType().ordinal()].put(content.name, content);
    }
}
