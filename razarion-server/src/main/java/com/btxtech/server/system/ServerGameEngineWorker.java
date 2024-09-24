package com.btxtech.server.system;

import com.btxtech.client.Event;
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

import javax.inject.Singleton;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by Beat
 * 07.01.2017.
 */
@Singleton
public class ServerGameEngineWorker extends GameEngineWorker {

    @Inject
    public ServerGameEngineWorker(NativeMatrixFactory nativeMatrixFactory, Provider<WorkerTrackerHandler> workerTrackerHandlerInstance, Provider<AbstractServerGameConnection> connectionInstance, TerrainService terrainService, ExceptionHandler exceptionHandler, PerfmonService perfmonService, GameLogicService logicService, CommandService commandService, BoxService boxService, QuestService questService, SyncItemContainerServiceImpl syncItemContainerService, BaseItemService baseItemService, ResourceService resourceService, BotService botService, Event<StaticGameInitEvent> staticGameInitEvent, PlanetService planetService) {
        super(nativeMatrixFactory, workerTrackerHandlerInstance, connectionInstance, terrainService, exceptionHandler, perfmonService, logicService, commandService, boxService, questService, syncItemContainerService, baseItemService, resourceService, botService, staticGameInitEvent, planetService);
    }

    @Override
    protected void sendToClient(GameEngineControlPackage.Command command, Object... object) {
        throw new UnsupportedOperationException();
    }
}
