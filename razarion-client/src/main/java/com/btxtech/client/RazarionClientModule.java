package com.btxtech.client;

import com.btxtech.client.gwtangular.GwtAngularFacade;
import com.btxtech.client.system.ClientServerSystemConnection;
import com.btxtech.client.system.boot.BootImpl;
import com.btxtech.common.system.ClientSimpleExecutorServiceImpl;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.ServerQuestProvider;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.control.AbstractServerSystemConnection;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.renderer.BabylonRenderServiceAccess;
import com.btxtech.uiservice.system.boot.Boot;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

import static com.btxtech.client.gwtangular.GwtAngularService.getGwtAngularFacade;

@Module
public abstract class RazarionClientModule {
    private static final GwtAngularFacade gwtAngularFacade = getGwtAngularFacade();

    @Provides
    public static BabylonRenderServiceAccess babylonRenderServiceAccess() {
        return gwtAngularFacade.babylonRenderServiceAccess;
    }

    @Provides
    public static ScreenCover screenCover() {
        return gwtAngularFacade.screenCover;
    }

    @Binds
    public abstract AbstractServerSystemConnection bindAabstractServerSystemConnection(ClientServerSystemConnection clientServerSystemConnection);

    @Binds
    public abstract GameEngineControl bindGameEngineControl(ClientGameEngineControl gameEngineControl);

    @Binds
    public abstract Boot bindBoot(BootImpl bootImpl);

    @Binds
    public abstract SimpleExecutorService bindSimpleExecutorService(ClientSimpleExecutorServiceImpl clientSimpleExecutorServiceImpl);

    @Binds
    public abstract AudioService bindAudioService(ClientAudioService clientAudioService);

    @Binds
    public abstract ServerQuestProvider bindServerQuestProvider(ClientServerQuestProvider clientServerQuestProvider);
}
