package com.btxtech.client.system;

import com.btxtech.client.gwtangular.GwtAngularService;
import com.btxtech.client.system.boot.GameStartupSeq;
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
        if (GwtAngularService.getGwtAngularFacade().screenCover != null) {
            count++;
            GwtAngularService.getGwtAngularFacade().screenCover.onStartupProgress(count / total * 100.0);
        }
    }

    @Override
    public void onStartupFinished(List<StartupTaskInfo> taskInfo, long totalTime) {
        GwtAngularService.getGwtAngularFacade().screenCover.removeLoadingCover();
    }

}
