package finalCampaign.feature.featureClass.spritePacker;

import arc.*;
import arc.files.*;
import arc.scene.ui.layout.*;
import mindustry.*;
import mindustry.graphics.*;

public class spritePackerDialog {
    private String rawAssetPath = "", outputPath = "";

    public spritePackerDialog(Table table) {
        table.add("Raw Asset Path").color(Pal.accent).left().wrap().width(500f);
        table.row();
        table.field("", t -> rawAssetPath = t).width(500f).padTop(10f);
        table.row();
        table.add("Output Path").color(Pal.accent).left().wrap().width(500f).padTop(8f);
        table.row();
        table.field("", t -> outputPath = t).width(500f).padTop(10f);
        table.row();
        table.button("Pack", () -> {
            Vars.ui.loadAnd(() -> {
                try {
                    spritePacker.pack(new Fi(rawAssetPath), new Fi(outputPath));
                    Vars.ui.showOkText("Pack", "A restart is needed to complete pack.", Core.app::exit);
                } catch(Exception e) {
                    Vars.ui.showException(e);
                }
            });
        }).width(100f).padTop(10f).center();
    }
}
