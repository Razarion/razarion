package com.btxtech.client;

import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.btxtech.shared.gameengine.datatypes.packets.UnlockResultInfo;
import com.btxtech.shared.rest.UnlockProvider;
import com.btxtech.uiservice.unlock.UnlockUiService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 23.09.2017.
 */
@ApplicationScoped
public class ClientUnlockUiService extends UnlockUiService {
    // private Logger logger = Logger.getLogger(ClientUnlockUiService.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private Caller<UnlockProvider> provider;

    @Override
    protected void unlockViaCrystalCall(LevelUnlockConfig levelUnlockConfig, Consumer<UnlockResultInfo> callback) {
        provider.call((RemoteCallback<UnlockResultInfo>) callback::accept, exceptionHandler.restErrorHandler("UnlockProvider.unlockViaCrystals() failed: message: ")).unlockViaCrystals(levelUnlockConfig.getId());
    }
}
