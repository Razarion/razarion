package com.btxtech.server.system;

import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.GameEngineWorker;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 07.01.2017.
 */
@ApplicationScoped
public class ServerGameEngineWorker extends GameEngineWorker {

    @Override
    protected void sendToClient(GameEngineControlPackage.Command command, Object... object) {
        throw new UnsupportedOperationException();
    }
}
