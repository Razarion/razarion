package com.btxtech.worker;

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
import com.btxtech.worker.jso.JsArray;
import com.btxtech.worker.jso.JsConsole;
import com.btxtech.worker.jso.JsMessageEvent;
import com.btxtech.worker.jso.JsUtils;
import com.btxtech.worker.jso.WorkerGlobalScope;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

/**
 * TeaVM implementation of the ClientGameEngineWorker
 * Replaces GWT JSNI with TeaVM @JSBody annotations
 */
@Singleton
public class TeaVMClientGameEngineWorker extends GameEngineWorker {
    private final TeaVMClientPerformanceTrackerService clientPerformanceTrackerService;

    @Inject
    public TeaVMClientGameEngineWorker(Provider<AbstractServerGameConnection> connectionInstance,
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
                                       TeaVMClientPerformanceTrackerService clientPerformanceTrackerService) {
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

    /**
     * Initialize the worker by setting up the message handler
     */
    public void init() {
        WorkerGlobalScope workerScope = WorkerGlobalScope.current();

        workerScope.setOnMessage(evt -> {
            try {
                JsMessageEvent messageEvent = (JsMessageEvent) evt;
                GameEngineControlPackage controlPackage = TeaVMWorkerMarshaller.deMarshall(messageEvent.getData());
                dispatch(controlPackage);
            } catch (Throwable t) {
                JsConsole.error("TeaVMClientGameEngineWorker: exception processing package on worker: " + t.getMessage());
            }
        });

        sendToClient(GameEngineControlPackage.Command.LOADED);
    }

    @Override
    public void start(String bearerToken) {
        super.start(bearerToken);
        clientPerformanceTrackerService.start();
    }

    @Override
    public void stop() {
        clientPerformanceTrackerService.stop();
        super.stop();
    }

    @Override
    protected void sendToClient(GameEngineControlPackage.Command command, Object... object) {
        JsArray<Object> message = TeaVMWorkerMarshaller.marshall(new GameEngineControlPackage(command, object));
        WorkerGlobalScope.current().postMessage(message);
    }

    @Override
    protected int[] convertIntArray(int[] intArray) {
        // TeaVM handles int[] differently than GWT - convert to JS array
        JsArray<Integer> jsArray = JsUtils.convertIntArray(intArray);
        // Return the original array since TeaVM properly handles primitive arrays
        return intArray;
    }
}
