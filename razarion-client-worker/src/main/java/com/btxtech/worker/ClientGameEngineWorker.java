package com.btxtech.worker;


import com.btxtech.common.WorkerMarshaller;
import com.btxtech.common.system.ClientPerformanceTrackerService;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.GameEngineWorker;
import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BoxService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.connection.AbstractServerGameConnection;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.system.perfmon.PerfmonService;
import elemental2.dom.DedicatedWorkerGlobalScope;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 30.12.2016.
 */
@Singleton
public class ClientGameEngineWorker extends GameEngineWorker {
    private final Logger logger = Logger.getLogger(ClientGameEngineWorker.class.getName());
    private final ClientPerformanceTrackerService clientPerformanceTrackerService;

    @Inject
    public ClientGameEngineWorker(Provider<AbstractServerGameConnection> connectionInstance,
                                  TerrainService terrainService,
                                  PerfmonService perfmonService,
                                  GameLogicService logicService,
                                  CommandService commandService,
                                  BoxService boxService,
                                  QuestService questService,
                                  SyncItemContainerServiceImpl syncItemContainerService,
                                  BaseItemService baseItemService,
                                  ResourceService resourceService,
                                  BotService botService,
                                  InitializeService initializeService,
                                  PlanetService planetService,
                                  ClientPerformanceTrackerService clientPerformanceTrackerService) {
        super(connectionInstance,
                terrainService,
                perfmonService,
                logicService,
                commandService,
                boxService,
                questService,
                syncItemContainerService,
                baseItemService,
                resourceService,
                botService,
                initializeService,
                planetService);
        this.clientPerformanceTrackerService = clientPerformanceTrackerService;
    }

    public static native DedicatedWorkerGlobalScope getDedicatedWorkerGlobalScope() /*-{
        return self;
    }-*/;

    public void init() {
        getDedicatedWorkerGlobalScope().setOnmessage(evt -> {
            try {
                GameEngineControlPackage controlPackage = WorkerMarshaller.deMarshall(evt.data);
                dispatch(controlPackage);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "ClientGameEngineWorker: exception processing package on worker. Data: " + evt.data, t);
            }
        });
        sendToClient(GameEngineControlPackage.Command.LOADED);
    }

    @Override
    public void start(String barerToken) {
        super.start(barerToken);
        clientPerformanceTrackerService.start();
    }

    @Override
    public void stop() {
        clientPerformanceTrackerService.stop();
        super.stop();
    }

    @Override
    protected void sendToClient(GameEngineControlPackage.Command command, Object... object) {
        getDedicatedWorkerGlobalScope().postMessage(WorkerMarshaller.marshall(new GameEngineControlPackage(command, object)));
    }

    @Override
    protected native int[] convertIntArray(int[] intArray) /*-{
        var result = [];
        for (i = 0; i < intArray.length; i++) {
            result[i] = intArray[i];
        }
        return result;
    }-*/;
}
