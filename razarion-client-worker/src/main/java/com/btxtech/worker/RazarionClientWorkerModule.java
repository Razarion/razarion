package com.btxtech.worker;

import com.btxtech.common.ClientNativeTerrainShapeAccess;
import com.btxtech.common.DummyClientSyncServer;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.common.system.ClientPerformanceTrackerService;
import com.btxtech.common.system.ClientSimpleExecutorServiceImpl;
import com.btxtech.common.system.WebSocketWrapper;
import com.btxtech.shared.deprecated.Event;
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
import com.btxtech.shared.gameengine.planet.SyncService;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.connection.AbstractServerGameConnection;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;
import com.btxtech.shared.nativejs.NativeMatrix;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.debugtool.DebugHelper;
import com.btxtech.shared.system.perfmon.PerfmonService;
import dagger.Module;
import dagger.Provides;

import javax.inject.Provider;

@Module
public class RazarionClientWorkerModule {
    @Provides
    public NativeMatrixFactory nativeMatrixFactory() {
        return new NativeMatrixFactory() {
            @Override
            public NativeMatrix createFromColumnMajorArray(double[] array) {
                return super.createFromColumnMajorArray(array);
            }
        }; // TODO
    }

    @Provides
    public SimpleExecutorService simpleExecutorService() {
        return new ClientSimpleExecutorServiceImpl(null);
    }

    @Provides
    public WorkerTrackerHandler workerTrackerHandler(SimpleExecutorService simpleExecutorService) {
        return new ClientWorkerTrackerHandler(simpleExecutorService);
    }

    @Provides
    public AbstractServerGameConnection abstractServerGameConnection(GameEngineWorker gameEngineWorker,
                                                                     WebSocketWrapper webSocketWrapper,
                                                                     PlanetService planetService,
                                                                     BoxService boxService,
                                                                     ResourceService resourceService,
                                                                     BaseItemService baseItemService) {
        return new ClientServerGameConnection(gameEngineWorker, webSocketWrapper, planetService, boxService, resourceService, baseItemService);
    }

    @Provides
    public ExceptionHandler exceptionHandler() {
        return new ClientExceptionHandlerImpl(null);
    }

    @Provides
    public NativeTerrainShapeAccess nativeTerrainShapeAccess(TerrainService terrainService) {
        return new ClientNativeTerrainShapeAccess(terrainService);
    }

    @Provides
    public SyncService SyncService() {
        return new DummyClientSyncServer();
    }
    @Provides
    public DebugHelper debugHelper() {
        return new DebugHelper() {
            @Override
            public void debugToDb(String debugMessage) {

            }

            @Override
            public void debugToConsole(String debugMessage) {

            }
        };
    }

    @Provides
    public GameEngineWorker gameEngineWorker(NativeMatrixFactory nativeMatrixFactory,
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
        return new ClientGameEngineWorker(nativeMatrixFactory,
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
                planetService,
                exceptionHandler1,
                clientPerformanceTrackerService);
    }
}

