package com.btxtech.worker;

import com.btxtech.common.ClientNativeTerrainShapeAccess;
import com.btxtech.common.DummyClientSyncServer;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.common.system.ClientSimpleExecutorServiceImpl;
import com.btxtech.shared.gameengine.GameEngineWorker;
import com.btxtech.shared.gameengine.WorkerTrackerHandler;
import com.btxtech.shared.gameengine.planet.SyncService;
import com.btxtech.shared.gameengine.planet.connection.AbstractServerGameConnection;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;
import com.btxtech.shared.nativejs.NativeMatrix;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.debugtool.DebugHelper;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class RazarionClientWorkerModule {
    @Provides
    public static NativeMatrixFactory nativeMatrixFactory() {
        return new NativeMatrixFactory() {
            @Override
            public NativeMatrix createFromColumnMajorArray(double[] array) {
                return super.createFromColumnMajorArray(array);
            }
        }; // TODO
    }


    @Provides
    public static DebugHelper debugHelper() {
        return new DebugHelper() {
            @Override
            public void debugToDb(String debugMessage) {

            }

            @Override
            public void debugToConsole(String debugMessage) {

            }
        };
    }

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
    public abstract ExceptionHandler bindExceptionHandler(ClientExceptionHandlerImpl exceptionHandlerImpl);

    @Binds
    public abstract SimpleExecutorService bindSimpleExecutorService(ClientSimpleExecutorServiceImpl clientSimpleExecutorServiceImpl);
}

