package finalCampaign.feature.hudUIInfo;

import arc.scene.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import finalCampaign.event.*;

public class logic {

    public static void buildUI(fcInputHandleBuildUIEvent event) {
        if (!fHudUIInfo.isLoaded() && !OS.isAndroid)
            return;

        Element[] children = event.group.getChildren().items;

        Table bottomHint = (Table) children[children.length - 1];
        Table schematicCtrlHint = (Table) children[children.length - 2];

        bottomHint.name = "fcBottomHint";
        schematicCtrlHint.name = "fcSchematicCtrlHint";
    }
}
