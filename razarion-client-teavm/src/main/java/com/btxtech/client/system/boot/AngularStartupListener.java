package com.btxtech.client.system.boot;

import com.btxtech.client.jso.JsConsole;
import com.btxtech.client.jso.facade.JsGwtAngularFacade;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.system.boot.StartupProgressListener;
import com.btxtech.uiservice.system.boot.StartupTaskEnum;
import com.btxtech.uiservice.system.boot.StartupTaskInfo;

import java.util.List;

public class AngularStartupListener implements StartupProgressListener {
    private final double total;
    private double count = 0;

    public AngularStartupListener(GameStartupSeq gameStartupSeq) {
        total = gameStartupSeq.getAbstractStartupTaskEnum().length;
    }

    @Override
    public void onNextTask(StartupTaskEnum taskEnum) {
        try {
            ScreenCover screenCover = JsGwtAngularFacade.get().getScreenCoverAdapter();
            if (screenCover != null) {
                count++;
                screenCover.onStartupProgress(count / total * 100.0);
            }
        } catch (Throwable t) {
            // Screen cover may not be available yet
        }
    }

    @Override
    public void onStartupFinished(List<StartupTaskInfo> taskInfo, long totalTime) {
        try {
            ScreenCover screenCover = JsGwtAngularFacade.get().getScreenCoverAdapter();
            if (screenCover != null) {
                screenCover.removeLoadingCover();
                JsConsole.log("[CLIENT-WASM] Loading cover removed");
            } else {
                JsConsole.error("[CLIENT-WASM] No screen cover to remove - the loading cover stays up");
            }
        } catch (Throwable t) {
            // Not silent any more. Whatever goes wrong here leaves the loading cover on top of a
            // running game, where it is invisible after its fade but still takes every touch -
            // and that had to be diagnosed from the outside, with a pointer probe, because
            // nothing here said a word.
            JsConsole.error("[CLIENT-WASM] Failed to remove the loading cover: " + t.getMessage());
        }
    }
}
