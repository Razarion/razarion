package com.btxtech.worker;

import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.GameEngineWorker;
import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
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
import com.btxtech.worker.jso.SharedTickBufferWriter;
import com.btxtech.worker.jso.WorkerGlobalScope;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

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
    private SharedTickBufferWriter sharedTickBufferWriter;

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
        JsConsole.log("[WORKER-WASM] Starting...");

        WorkerGlobalScope workerScope = WorkerGlobalScope.current();

        workerScope.setOnMessage(evt -> {
            try {
                JsMessageEvent messageEvent = (JsMessageEvent) evt;
                JSObject data = messageEvent.getData();
                // Check if this is the SharedArrayBuffer init message
                if (isSharedTickBufferInit(data)) {
                    JSObject sab = getSharedBuffer(data);
                    sharedTickBufferWriter = new SharedTickBufferWriter(sab);
                    JsConsole.log("[WORKER-WASM] SharedArrayBuffer tick writer initialized");
                    return;
                }
                GameEngineControlPackage controlPackage = TeaVMWorkerMarshaller.deMarshall(data);
                dispatch(controlPackage);
            } catch (Throwable t) {
                JsConsole.error("[WORKER-WASM] Exception processing package: " + t.getMessage());
            }
        });

        sendToClient(GameEngineControlPackage.Command.LOADED);
        JsConsole.log("[WORKER-WASM] ✓ Initialized successfully");
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

    @Override
    protected boolean isSharedBufferMode() {
        return sharedTickBufferWriter != null;
    }

    @Override
    protected void writeTickToSharedBuffer(NativeTickInfo nativeTickInfo) {
        sharedTickBufferWriter.writeTick(nativeTickInfo);
    }

    @JSBody(params = {"data"}, script = "return data && data.type === 'shared-tick-buffer' && data.buffer instanceof SharedArrayBuffer;")
    private static native boolean isSharedTickBufferInit(JSObject data);

    @JSBody(params = {"data"}, script = "return data.buffer;")
    private static native JSObject getSharedBuffer(JSObject data);
}
