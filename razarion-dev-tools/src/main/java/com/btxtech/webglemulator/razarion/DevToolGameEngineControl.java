package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.uiservice.control.GameEngineControl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.01.2017.
 */
@ApplicationScoped
public class DevToolGameEngineControl extends GameEngineControl {
    @Inject
    private WorkerEmulator workerEmulator;

    @Override
    protected void sendToWorker(GameEngineControlPackage.Command command, Object... data) {
        workerEmulator.sendPackage(new GameEngineControlPackage(command, data));
    }

    void receivePackage(GameEngineControlPackage gameEngineControlPackage) {
        dispatch(gameEngineControlPackage);
    }
}
