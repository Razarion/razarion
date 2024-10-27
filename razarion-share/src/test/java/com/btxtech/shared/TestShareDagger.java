package com.btxtech.shared;

import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.gui.DaggerTestRenderer;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import com.btxtech.shared.mock.TestNativeTerrainShapeAccess;
import com.btxtech.shared.mock.TestSimpleExecutorService;
import com.btxtech.shared.system.alarm.AlarmService;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = TestSharedDaggerModule.class)
public interface TestShareDagger {
    TestSimpleExecutorService testSimpleExecutorService();

    BaseItemService baseItemService();

    PlanetService planetService();

    AlarmService alarmService();

    GameLogicService gameLogicService();

    InitializeService initializeService();

    TestNativeTerrainShapeAccess testNativeTerrainShapeAccess();

    SyncItemContainerServiceImpl syncItemContainerService();

    PathingService pathingService();

    DaggerTestRenderer daggerTestRenderer();
}
