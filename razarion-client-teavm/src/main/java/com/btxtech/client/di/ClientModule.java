package com.btxtech.client.di;

import com.btxtech.client.TeaVMBootImpl;
import com.btxtech.client.TeaVMClientAudioService;
import com.btxtech.client.TeaVMClientGameEngineControl;
import com.btxtech.client.TeaVMClientServerQuestProvider;
import com.btxtech.client.TeaVMClientServerSystemConnection;
import com.btxtech.client.TeaVMGwtAngularService;
import com.btxtech.client.TeaVMHeightMapConverter;
import com.btxtech.client.TeaVMStatusProvider;
import com.btxtech.client.jso.facade.JsGwtAngularFacade;
import com.btxtech.client.system.TeaVMSimpleExecutorServiceImpl;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.ServerQuestProvider;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.control.AbstractServerSystemConnection;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.renderer.BabylonRenderServiceAccess;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.terrain.HeightMapConverter;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class ClientModule {

    @Provides
    public static BabylonRenderServiceAccess babylonRenderServiceAccess() {
        return JsGwtAngularFacade.get().getBabylonRenderServiceAccessAdapter();
    }

    @Provides
    public static ScreenCover screenCover() {
        return JsGwtAngularFacade.get().getScreenCoverAdapter();
    }

    @Binds
    public abstract AbstractServerSystemConnection bindAbstractServerSystemConnection(
            TeaVMClientServerSystemConnection connection);

    @Binds
    public abstract GameEngineControl bindGameEngineControl(
            TeaVMClientGameEngineControl gameEngineControl);

    @Binds
    public abstract Boot bindBoot(TeaVMBootImpl bootImpl);

    @Binds
    public abstract SimpleExecutorService bindSimpleExecutorService(
            TeaVMSimpleExecutorServiceImpl simpleExecutorService);

    @Binds
    public abstract AudioService bindAudioService(
            TeaVMClientAudioService audioService);

    @Binds
    public abstract ServerQuestProvider bindServerQuestProvider(
            TeaVMClientServerQuestProvider questProvider);

    @Binds
    public abstract HeightMapConverter bindHeightMapConverter(
            TeaVMHeightMapConverter heightMapConverter);
}
