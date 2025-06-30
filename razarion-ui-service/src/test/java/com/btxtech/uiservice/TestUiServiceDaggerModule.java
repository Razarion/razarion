package com.btxtech.uiservice;

import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.mock.BabylonRenderServiceAccessMock;
import com.btxtech.uiservice.mock.TestAudioService;
import com.btxtech.uiservice.mock.TestBootImpl;
import com.btxtech.uiservice.mock.TestGameEngineControl;
import com.btxtech.uiservice.mock.TestScreenCover;
import com.btxtech.uiservice.mock.TestServerQuestProvider;
import com.btxtech.uiservice.mock.TestServerSystemConnection;
import com.btxtech.uiservice.mock.TestSimpleExecutorService;
import com.btxtech.uiservice.mock.TestTrackerService;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.control.AbstractServerSystemConnection;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.renderer.BabylonRenderServiceAccess;
import com.btxtech.uiservice.system.boot.Boot;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class TestUiServiceDaggerModule {
    @Binds
    public abstract BabylonRenderServiceAccess bindBabylonRenderServiceAccess(BabylonRenderServiceAccessMock babylonRenderServiceAccessMock);

    @Binds
    public abstract ScreenCover bindScreenCover(TestScreenCover testScreenCover);

    @Binds
    public abstract AbstractServerSystemConnection bindAabstractServerSystemConnection(TestServerSystemConnection testServerSystemConnection);

    @Binds
    public abstract GameEngineControl bindGameEngineControl(TestGameEngineControl testGameEngineControl);

    @Binds
    public abstract Boot bindBoot(TestBootImpl testBootImpl);

    @Binds
    public abstract SimpleExecutorService bindSimpleExecutorService(TestSimpleExecutorService testSimpleExecutorService);

    @Binds
    public abstract AudioService bindAudioService(TestAudioService testAudioService);

    @Binds
    public abstract TrackerService bindTrackerService(TestTrackerService testTrackerService);

    @Binds
    public abstract ServerQuestProvider bindServerQuestProvider(TestServerQuestProvider testServerQuestProvider);

}
