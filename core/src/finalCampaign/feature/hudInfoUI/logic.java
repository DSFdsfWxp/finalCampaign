package finalCampaign.feature.hudInfoUI;

import arc.scene.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import finalCampaign.event.*;

public class logic {
    private static Table bottomHint, schematicCtrlHint;

    public static void buildUI(fcInputHandleBuildUIEvent event) {
        if (!fHudInfoUI.isLoaded() && !OS.isAndroid)
            return;

        Element[] children = event.group.getChildren().items;

        bottomHint = (Table) children[children.length - 1];
        schematicCtrlHint = (Table) children[children.length - 2];

        bottomHint.name = "fcBottomHint";
        schematicCtrlHint.name = "fcSchematicCtrlHint";
    }

    public static float calcHintHeight() {
        if (!fHudInfoUI.isLoaded() && !OS.isAndroid)
            return 0f;

        float res = 0f;

        res += bottomHint.getChildren().sumf(e -> e instanceof Table t ? t.getHeight() : 0f);
        res += schematicCtrlHint.getChildren().sumf(e -> e instanceof Table t ? t.getHeight() : 0f);

        return res;
    }
}
