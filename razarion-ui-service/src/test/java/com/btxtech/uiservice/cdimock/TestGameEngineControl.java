package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.uiservice.SelectionService;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.inventory.InventoryUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.terrain.InputService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.uiservice.user.UserUiService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.logging.Logger;

import static com.btxtech.shared.gameengine.GameEngineControlPackage.Command.TERRAIN_TILE_REQUEST;
import static com.btxtech.shared.gameengine.GameEngineControlPackage.Command.TERRAIN_TILE_RESPONSE;

/**
 * Created by Beat
 * 24.01.2017.
 */
@Singleton
public class TestGameEngineControl extends GameEngineControl {
    private final Logger logger = Logger.getLogger(TestGameEngineControl.class.getName());

    @Inject
    public TestGameEngineControl(Provider<InputService> inputServices,
                                 PerfmonService perfmonService,
                                 Provider<Boot> boot,
                                 TerrainUiService terrainUiService,
                                 InventoryUiService inventoryUiService,
                                 UserUiService userUiService,
                                 SelectionService selectionHandler,
                                 GameUiControl gameUiControl,
                                 AudioService audioService,
                                 BoxUiService boxUiService,
                                 ResourceUiService resourceUiService,
                                 BaseItemUiService baseItemUiService) {
        super(inputServices,
                perfmonService,
                boot,
                terrainUiService,
                inventoryUiService,
                userUiService,
                selectionHandler,
                gameUiControl,
                audioService,
                boxUiService,
                resourceUiService,
                baseItemUiService);
    }

    @Override
    public boolean isStarted() {
        logger.fine("isStarted()");
        return true;
    }

    @Override
    protected void sendToWorker(GameEngineControlPackage.Command command, Object... data) {
        logger.info("sendToWorker(): " + command + " data: " + data);
        if (command == TERRAIN_TILE_REQUEST) {
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
