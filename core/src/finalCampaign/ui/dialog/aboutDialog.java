package finalCampaign.ui.dialog;

import arc.*;
import finalCampaign.*;
import finalCampaign.feature.wiki.*;
import mindustry.*;
import mindustry.core.*;
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
        cont.add(version.inPackage.getVersionFull("mod")).color(Pal.lightishGray).row();
        cont.add(bundle.get("dialog.about.version.launcher")).color(Pal.accent).row();
        cont.add(finalCampaign.runtime.getVersion()).color(Pal.lightishGray).row();
        cont.add(bundle.get("dialog.about.version.runtime")).color(Pal.accent).row();
        cont.add(finalCampaign.runtime.name()).color(Pal.lightishGray).row();

        buttons.button(bundle.get("dialog.about.changeLog"), () -> {
            fWiki.show("changeLog");
        });

        buttons.button(bundle.get("dialog.about.openRepo"), () -> {
            Core.app.openURI("finalCampaign://repo");
        });
    }
}
