package com.btxtech.shared;
import com.btxtech.shared.gameengine.planet.SyncService;
import com.btxtech.shared.gameengine.planet.TestSyncService;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;
import com.btxtech.shared.mock.TestExceptionHandler;
import com.btxtech.shared.mock.TestNativeTerrainShapeAccess;
import com.btxtech.shared.mock.TestSimpleExecutorService;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class TestSharedDaggerModule {

    @Binds
    public abstract SimpleExecutorService bindSimpleExecutorService(TestSimpleExecutorService testSimpleExecutorService);

    @Binds
    public abstract SyncService bindSyncService(TestSyncService testSyncService);

    @Binds
    public abstract ExceptionHandler bindExceptionHandler(TestExceptionHandler testExceptionHandler);

    @Binds
    public abstract NativeTerrainShapeAccess bindNativeTerrainShapeAccess(TestNativeTerrainShapeAccess testNativeTerrainShapeAccess);

}
