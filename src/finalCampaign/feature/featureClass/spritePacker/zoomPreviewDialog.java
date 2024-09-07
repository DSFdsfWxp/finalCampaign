package finalCampaign.feature.featureClass.spritePacker;

import arc.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import finalCampaign.ui.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.ui.dialogs.*;

public class zoomPreviewDialog extends BaseDialog {
    Table imgTable;
    TextField urlField;
    Pixmap pixmap;
    Texture texture;

    zoomPreviewDialog() {
        super("Zoom Preview");
        addCloseButton();
        addCloseListener();
        buildUI();
    }

    private void buildUI() {
        imgTable = cont.table().height(320f).padBottom(50f).get();
        imgTable.image(Core.atlas.find("error")).size(32f).scaling(Scaling.fit).get();
        cont.row();
        
        Label sizeLabel = cont.add("32px * " + Float.toString(Scl.scl())).padTop(8f).get();

        barSetter scl = new barSetter("Scroll", 400f, 10f, 0.01f, 1, false, false, false, true, true);
        cont.row();
        scl.modified(() -> {
            float s = 32f * scl.value();
            sizeLabel.setText(Float.toString(s) + "px * " + Float.toString(Scl.scl()));
            imgTable.clear();
            imgTable.image(texture == null ? Core.atlas.find("error") : new TextureRegion(texture)).size(Scl.scl(s));
        });
        cont.add(scl).expandX().center().padTop(8f).row();

        cont.table(urlTable -> {
            urlTable.add("Url: ").grow();
            urlField = urlTable.field("", txt -> {}).width(300f).get();
            urlTable.button(Icon.cancel, () -> urlField.clearText()).width(45f).padLeft(4f);
        }).expandX().center().padTop(8f).width(400f).row();

        cont.button("Load", () -> {
            String url = urlField.getText().trim();
            if (url.startsWith("\"")) url = url.substring(1);
            if (url.endsWith("\"")) url = url.substring(0, url.length() - 1);
            if (url.isEmpty()) return;

            try {
                if (pixmap != null && !pixmap.isDisposed()) pixmap.dispose();
                if (texture != null && !texture.isDisposed()) texture.dispose();
    
                pixmap = new Pixmap(url);
                texture = new Texture(pixmap);
    
                texture.setFilter(TextureFilter.linear);
                scl.modify();
            } catch(Exception e) {
                Vars.ui.showException(e);
            }
        }).width(100f).padTop(10f);
    }
}
