package finalCampaign.dialog;

import arc.*;
import finalCampaign.*;
import finalCampaign.launch.*;
import mindustry.Vars;
import mindustry.core.Version;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

public class aboutDialog extends BaseDialog {
    public aboutDialog() {
        super(bundle.get("dialog.about.title"));
        addCloseButton();
        addCloseListener();

        BorderImage img = new BorderImage(Icon.book);
        img.border(Pal.accent);
        cont.add(img).size(102f).row();
        cont.add("[accent]FinalCampaign").padBottom(10f).row();

        cont.add(bundle.get("dialog.about.version.game")).color(Pal.accent).row();
        cont.add((Vars.steam ? "[Steam]" : "") + "[" + Version.type + "] " + Version.modifier + "-" + Version.number + "-" + Version.build).color(Pal.lightishGray).row();
        cont.add(bundle.get("dialog.about.version.mod")).color(Pal.accent).row();
        cont.add(version.toVersionString()).color(Pal.lightishGray).row();
        cont.add(bundle.get("dialog.about.version.launcher")).color(Pal.accent).row();
        cont.add(bothLauncherVersion.toVersionString()).color(Pal.lightishGray).row();

        buttons.button(bundle.get("dialog.about.openRepo"), () -> {
            Core.app.openURI("finalCampaign://repo");
        });
    }
}
