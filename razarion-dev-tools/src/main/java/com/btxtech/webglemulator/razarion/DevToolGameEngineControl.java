package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.dto.VertexList;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.planet.terrain.Water;
import com.btxtech.shared.gameengine.planet.terrain.slope.Mesh;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.webglemulator.razarion.renderer.DevToolFloat32ArrayEmu;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 04.01.2017.
 */
@ApplicationScoped
public class DevToolGameEngineControl extends GameEngineControl {
    @Inject
    private WorkerEmulator workerEmulator;

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    protected void sendToWorker(GameEngineControlPackage.Command command, Object... data) {
        workerEmulator.sendPackage(new GameEngineControlPackage(command, data));
    }

    void receivePackage(GameEngineControlPackage gameEngineControlPackage) {
        dispatch(gameEngineControlPackage);
    }

    @Override
    protected void onLoaded() {
        throw new UnsupportedOperationException();
    }
}
