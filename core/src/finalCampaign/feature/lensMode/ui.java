package finalCampaign.feature.lensMode;

import arc.func.*;
import arc.scene.ui.*;
import finalCampaign.*;
import finalCampaign.feature.hudUI.*;
import finalCampaign.ui.*;
import mindustry.*;
import mindustry.input.*;

public class ui {

    private static Runnable closeSpeedSetToast;
    private static roulette modeRoulette;

    public static void initRoulette() {
        modeRoulette = new roulette();
        modeRoulette.setUsingFourSlot(true);
        for (var mode : fLensMode.lensMode.values()) {
            String name = bundle.get("lensMode." + fLensMode.mode.name() + ".name");
            var icon = new Image(atlas.find("icon-" + mode.name()));
            Boolp valid = () -> !(Vars.control.input instanceof MobileInput && mode == fLensMode.lensMode.defaultCamera);

            modeRoulette.addRouletteChoice(icon, mode, name, valid, () -> {
                if (valid.get()) {
                    fLensMode.setMode(mode);
                    showModeChangeToast();
                }
            });
        }
    }

    public static void makeSureRouletteShown() {
        if (!modeRoulette.isShown())
            modeRoulette.showRoulette(fHudUI.topPopupLayer);
    }

    public static void closeRoulette() {
        if (modeRoulette.isShown())
            modeRoulette.closeRoulette();
    }

    public static void makeSureSpeedSetToastOpen() {
        if (closeSpeedSetToast == null)
            fHudUI.showBottomToast((t, close) -> {
                closeSpeedSetToast = close;
                t.label(() -> bundle.format("lensMode.speedSetToast", Integer.toString((int) Math.ceil(fLensMode.panSpeedPercent * 100f))));
            });
    }

    public static void closeSpeedSetToast() {
        if (closeSpeedSetToast != null) {
            closeSpeedSetToast.run();
            closeSpeedSetToast = null;
        }
    }

    public static void showModeChangeToast() {
        fHudUI.showBottomToast(bundle.format("lensMode.currentMode", bundle.get("lensMode." + fLensMode.mode.name() + ".name")));
    }
}
