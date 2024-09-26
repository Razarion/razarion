package com.btxtech.client;

import com.btxtech.client.gwtangular.GwtAngularFacade;
import com.btxtech.client.system.ClientServerSystemConnection;
import com.btxtech.client.system.boot.BootImpl;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.common.system.ClientSimpleExecutorServiceImpl;
import com.btxtech.shared.nativejs.NativeMatrix;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.ServerQuestProvider;
import com.btxtech.uiservice.TrackerService;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.control.AbstractServerSystemConnection;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.renderer.BabylonRenderServiceAccess;
import com.btxtech.uiservice.system.boot.Boot;
import dagger.Module;
import dagger.Provides;

import javax.inject.Inject;

import static com.btxtech.client.gwtangular.GwtAngularService.getGwtAngularFacade;

@Module
public class RazarionClientModule {
    private final GwtAngularFacade gwtAngularFacade;

    @Inject
    public RazarionClientModule() {
        this.gwtAngularFacade = getGwtAngularFacade();
    }

    @Provides
    public SimpleExecutorService simpleExecutorService() {
        return new ClientSimpleExecutorServiceImpl(null);
    }

    @Provides
    public AudioService sudioService() {
        return new ClientAudioService(null, null, null, null);
    }

    @Provides
    public GameEngineControl gameEngineControl() {
        return new ClientGameEngineControl(null, null, null, null, null, null, null, null, null, null, null, null, () -> null, null);
    }

    @Provides
    public ExceptionHandler exceptionHandler() {
        return new ClientExceptionHandlerImpl(null);
    }

    @Provides
    public Boot boot() {
        return new BootImpl(null, null, null);
    }

    @Provides
    public BabylonRenderServiceAccess babylonRenderServiceAccess() {
        return gwtAngularFacade.babylonRenderServiceAccess;
    }

    @Provides
    public TrackerService trackerService() {
        return new ClientTrackerService(null, null, null, null);
    }

    @Provides
    public NativeMatrixFactory bativeMatrixFactory() {
        return new NativeMatrixFactory() {
            @Override
            public NativeMatrix createFromColumnMajorArray(double[] array) {
                return super.createFromColumnMajorArray(array);
            }
        }; // TODO
    }

    @Provides
    public AbstractServerSystemConnection abstractServerSystemConnection() {
        return new ClientServerSystemConnection(null, null, null, null, null, null, null, null);
    }

    @Provides
    public ServerQuestProvider serverQuestProvider() {
        return new ClientServerQuestProvider(null, null);
    }

    @Provides
    public ScreenCover screenCover() {
        return gwtAngularFacade.screenCover;
    }
}
