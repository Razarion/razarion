package com.btxtech.worker;

import com.btxtech.common.ClientNativeTerrainShapeAccess;
import com.btxtech.common.DummyClientSyncServer;
import com.btxtech.common.system.ClientSimpleExecutorServiceImpl;
import com.btxtech.shared.gameengine.GameEngineWorker;
import com.btxtech.shared.gameengine.WorkerTrackerHandler;
import com.btxtech.shared.gameengine.planet.SyncService;
import com.btxtech.shared.gameengine.planet.connection.AbstractServerGameConnection;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;
import com.btxtech.shared.system.SimpleExecutorService;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class RazarionClientWorkerModule {

    @Binds
    public abstract WorkerTrackerHandler bindWorkerTrackerHandler(ClientWorkerTrackerHandler workerTrackerHandler);

    @Binds
    public abstract AbstractServerGameConnection bindAbstractServerGameConnection(ClientServerGameConnection clientServerGameConnection);

    @Binds
    public abstract NativeTerrainShapeAccess bindNativeTerrainShapeAccess(ClientNativeTerrainShapeAccess clientNativeTerrainShapeAccess);

    @Binds
    public abstract SyncService bindSyncService(DummyClientSyncServer dummyClientSyncServer);

    @Binds
    public abstract GameEngineWorker bindGameEngineWorker(ClientGameEngineWorker clientGameEngineWorker);

    @Binds
    public abstract SimpleExecutorService bindSimpleExecutorService(ClientSimpleExecutorServiceImpl clientSimpleExecutorServiceImpl);
}

