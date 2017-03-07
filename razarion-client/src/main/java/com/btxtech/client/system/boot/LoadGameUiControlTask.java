package com.btxtech.client.system.boot;

import com.btxtech.client.editor.terrain.TerrainEditorImpl;
import com.btxtech.shared.dto.FacebookUserLoginInfo;
import com.btxtech.shared.dto.GameUiControlConfig;
import com.btxtech.shared.rest.GameUiControlProvider;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.uiservice.user.UserUiService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 07.02.2016.
 */
@Dependent
public class LoadGameUiControlTask extends AbstractStartupTask {
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private Caller<GameUiControlProvider> serviceCaller;
    @Inject
    private UserUiService userUiService;
    private Logger logger = Logger.getLogger(LoadGameUiControlTask.class.getName());

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        FacebookUserLoginInfo facebookUserLoginInfo = userUiService.getFacebookUserLoginInfo();
        if(facebookUserLoginInfo == null) {
            facebookUserLoginInfo = new FacebookUserLoginInfo(); // Errai Jackson JAX-RS does not accept null value in POST rest call
        }
        serviceCaller.call(new RemoteCallback<GameUiControlConfig>() {
            @Override
            public void callback(GameUiControlConfig gameUiControlConfig) {
                gameUiControl.setGameUiControlConfig(gameUiControlConfig);
                deferredStartup.finished();
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "loadSlopeSkeletons failed: " + message, throwable);
            deferredStartup.failed(throwable);
            return false;
        }).loadGameUiControlConfig(facebookUserLoginInfo);
    }
}
