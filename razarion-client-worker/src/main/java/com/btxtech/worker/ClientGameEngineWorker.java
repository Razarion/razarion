package com.btxtech.worker;


import com.btxtech.shared.deprecated.Event;
import com.btxtech.common.WorkerMarshaller;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.common.system.ClientPerformanceTrackerService;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.GameEngineWorker;
import com.btxtech.shared.gameengine.StaticGameInitEvent;
import com.btxtech.shared.gameengine.WorkerTrackerHandler;
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
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.perfmon.PerfmonService;
import elemental2.dom.DedicatedWorkerGlobalScope;
import elemental2.dom.MessageEvent;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 30.12.2016.
 */
public class ClientGameEngineWorker extends GameEngineWorker {
    private final Logger logger = Logger.getLogger(ClientGameEngineWorker.class.getName());
    private final ClientExceptionHandlerImpl exceptionHandler;
    private final ClientPerformanceTrackerService clientPerformanceTrackerService;

    @Inject
    public ClientGameEngineWorker(NativeMatrixFactory nativeMatrixFactory,
                                  Provider<WorkerTrackerHandler> workerTrackerHandlerInstance,
                                  Provider<AbstractServerGameConnection> connectionInstance,
                                  TerrainService terrainService,
                                  ExceptionHandler exceptionHandler,
                                  PerfmonService perfmonService,
                                  GameLogicService logicService,
                                  CommandService commandService,
                                  BoxService boxService,
                                  QuestService questService,
                                  SyncItemContainerServiceImpl syncItemContainerService,
                                  BaseItemService baseItemService,
                                  ResourceService resourceService,
                                  BotService botService,
                                  Event<StaticGameInitEvent> staticGameInitEvent,
                                  PlanetService planetService,
                                  ClientExceptionHandlerImpl exceptionHandler1,
                                  ClientPerformanceTrackerService clientPerformanceTrackerService) {
        super(nativeMatrixFactory,
                workerTrackerHandlerInstance,
                connectionInstance,
                terrainService,
                exceptionHandler,
                perfmonService,
                logicService,
                commandService,
                boxService,
                questService,
                syncItemContainerService,
                baseItemService,
                resourceService,
                botService,
                staticGameInitEvent,
                planetService);
        this.exceptionHandler = exceptionHandler1;
        this.clientPerformanceTrackerService = clientPerformanceTrackerService;
    }

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
    public void start() {
        super.start();
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

    public static native DedicatedWorkerGlobalScope getDedicatedWorkerGlobalScope() /*-{
        return self;
    }-*/;
}
