package com.btxtech.client.dialog.common;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.client.user.Facebook;
import com.btxtech.shared.datatypes.RegisterInfo;
import com.btxtech.shared.rest.UserServiceProvider;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.user.UserUiService;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 26.12.2017.
 */
@Templated("RegisterDialog.html#registerDialog")
public class RegisterDialog extends Composite implements ModalDialogContent<Void> {
    private Logger logger = Logger.getLogger(RegisterDialog.class.getName());
    @Inject
    private Caller<UserServiceProvider> caller;
    @Inject
    private UserUiService userUiService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private ModalDialogPanel<Void> modalDialogPanel;

    @Override
    public void init(Void aVoid) {
        userUiService.clearRegisterTimer();
    }

    @Override
    public void customize(ModalDialogPanel<Void> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }

    @Override
    public void onShown() {
        try {
            Facebook.getFB().getEvent().subscribe("auth.statusChange", response -> {
                if (Facebook.CONNECTED.equalsIgnoreCase(response.status)) {
                    caller.call((RemoteCallback<RegisterInfo>) registerInfo -> {
                                if (registerInfo.isUserAlreadyExits()) {
                                    // User has been logged in on the server
                                    Window.Location.reload();
                                } else {
                                    userUiService.onUserRegistered(registerInfo.getHumanPlayerId());
                                }
                            },
                            (message, throwable) -> {
                                logger.log(Level.SEVERE, "RegisterDialog.inGameFacebookRegister() failed: " + message, throwable);
                                return false;
                            }).inGameFacebookRegister(Facebook.toFbAuthResponse(response.authResponse));
                    modalDialogPanel.close();
                }
            });
            // Renders the facebook register button
            Facebook.getFB().getXFBML().parse();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    public void onClose() {
        userUiService.activateRegisterTimer();
    }
}
