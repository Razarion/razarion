package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.GameEngineWorker;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.01.2017.
 */
@ApplicationScoped
public class DevToolGameEngineWorker extends GameEngineWorker {
    @Inject
    private DevToolGameEngineControl gameEngineControl;

    @Override
    protected void dispatchPackage(GameEngineControlPackage.Command command) {
        gameEngineControl.receivePackage(new GameEngineControlPackage(command, null));
    }

    void receivePackage(GameEngineControlPackage gameEngineControlPackage) {
        dispatch(gameEngineControlPackage);
    }
}
