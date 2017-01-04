package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.uiservice.storyboard.GameEngineControl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.01.2017.
 */
@ApplicationScoped
public class DevToolGameEngineControl extends GameEngineControl {
    @Inject
    private DevToolGameEngineWorker gameEngineWorker;

    @Override
    protected void sendToWorker(GameEngineControlPackage.Command command, Object data) {
        gameEngineWorker.receivePackage(new GameEngineControlPackage(command, data));
    }

    void receivePackage(GameEngineControlPackage gameEngineControlPackage) {
        dispatch(gameEngineControlPackage);
    }
}
