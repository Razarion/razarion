package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.uiservice.control.GameEngineControl;

import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

import static com.btxtech.shared.gameengine.GameEngineControlPackage.Command.TERRAIN_TILE_REQUEST;
import static com.btxtech.shared.gameengine.GameEngineControlPackage.Command.TERRAIN_TILE_RESPONSE;

/**
 * Created by Beat
 * 24.01.2017.
 */
@ApplicationScoped
public class TestGameEngineControl extends GameEngineControl {
    private final Logger logger = Logger.getLogger(TestGameEngineControl.class.getName());

    @Override
    public boolean isStarted() {
        logger.fine("isStarted()");
        return true;
    }

    @Override
    protected void sendToWorker(GameEngineControlPackage.Command command, Object... data) {
        logger.info("sendToWorker(): " + command + " data: " + data);
        if(command == TERRAIN_TILE_REQUEST) {
            TerrainTile terrainTile = new TerrainTile();
            terrainTile.setIndex((Index) data[0]);
            dispatch(new GameEngineControlPackage(TERRAIN_TILE_RESPONSE, terrainTile));
        }
    }

    @Override
    protected void onLoaded() {
        logger.fine("onLoaded()");
    }

    @Override
    protected NativeTickInfo castToNativeTickInfo(Object javaScriptObject) {
        logger.fine("castToNativeTickInfo()");
        return (NativeTickInfo) javaScriptObject;
    }

    @Override
    protected NativeSyncBaseItemTickInfo castToNativeSyncBaseItemTickInfo(Object singleData) {
        logger.fine("castToNativeSyncBaseItemTickInfo()");
        return (NativeSyncBaseItemTickInfo) singleData;
    }

    @Override
    protected void onConnectionLost() {
        logger.fine("onConnectionLost()");
    }
}
