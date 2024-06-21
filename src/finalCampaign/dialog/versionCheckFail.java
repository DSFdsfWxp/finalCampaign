package finalCampaign.dialog;

import arc.*;
import arc.files.*;
import arc.scene.ui.layout.*;
import finalCampaign.*;
import mindustry.Vars;
import mindustry.gen.*;
import mindustry.ui.dialogs.*;

public class versionCheckFail extends BaseDialog{
    public versionCheckFail(Fi mindustryClassFile) {
        super(bundle.get("error"));

        super.addCloseListener();
        super.buttons.defaults().size(210f, 64f);

        super.hidden(() -> {
            Core.app.exit();
        });
        super.buttons.button(bundle.get("exit"), Icon.exit, () -> {
            Core.app.exit();
        });

        super.buttons.button(bundle.get("dialog.versionCheckFail.importJar.button"), Icon.download, () -> {
            Vars.platform.showFileChooser(true, bundle.get("dialog.versionCheckFail.importJar.title"), "jar", (Fi file) -> {
                file.copyTo(mindustryClassFile);
                Vars.ui.showOkText(bundle.get("info"), bundle.get("dialog.versionCheckFail.importJar.successMsg"), () -> {
                    Core.app.exit();
                });
            });
        });

        super.cont.pane((Table t) -> {
            t.add(bundle.get("dialog.versionCheckFail.title")).left();
            t.row();
            t.add(bundle.get("dialog.versionCheckFail.detail")).padTop(10f).left().width(400f).wrap().grow();
            t.row();
            t.add(bundle.get("dialog.versionCheckFail.solution")).left().width(400f).wrap().grow();
        }).scrollX(false);
    }
}
