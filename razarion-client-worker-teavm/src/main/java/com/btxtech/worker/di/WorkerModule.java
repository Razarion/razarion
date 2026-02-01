package com.btxtech.worker.di;

import com.btxtech.shared.gameengine.GameEngineWorker;
import com.btxtech.shared.gameengine.planet.SyncService;
import com.btxtech.shared.gameengine.planet.connection.AbstractServerGameConnection;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.worker.TeaVMClientGameEngineWorker;
import com.btxtech.worker.TeaVMClientServerGameConnection;
import com.btxtech.worker.TeaVMDummyClientSyncServer;
import com.btxtech.worker.TeaVMNativeTerrainShapeAccess;
import com.btxtech.worker.TeaVMSimpleExecutorServiceImpl;
import dagger.Binds;
import dagger.Module;

/**
 * Dagger module for the TeaVM Web Worker
 * Binds interfaces to their TeaVM-specific implementations
 */
@Module
public abstract class WorkerModule {

    @Binds
    public abstract AbstractServerGameConnection bindAbstractServerGameConnection(
            TeaVMClientServerGameConnection clientServerGameConnection);

    @Binds
    public abstract NativeTerrainShapeAccess bindNativeTerrainShapeAccess(
            TeaVMNativeTerrainShapeAccess nativeTerrainShapeAccess);

    @Binds
    public abstract SyncService bindSyncService(
            TeaVMDummyClientSyncServer dummyClientSyncServer);

    @Binds
    public abstract GameEngineWorker bindGameEngineWorker(
            TeaVMClientGameEngineWorker clientGameEngineWorker);

    @Binds
    public abstract SimpleExecutorService bindSimpleExecutorService(
            TeaVMSimpleExecutorServiceImpl simpleExecutorService);
}
