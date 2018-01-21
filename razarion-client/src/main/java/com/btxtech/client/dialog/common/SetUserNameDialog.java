package com.btxtech.client.dialog.common;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.datatypes.ErrorResult;
import com.btxtech.shared.datatypes.SetNameResult;
import com.btxtech.shared.rest.UserServiceProvider;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.user.UserUiService;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Input;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 26.12.2017.
 */
@Templated("SetUserNameDialog.html#setNameDialog")
public class SetUserNameDialog extends Composite implements ModalDialogContent<Void> {
    private Logger logger = Logger.getLogger(SetUserNameDialog.class.getName());
    @Inject
    private Caller<UserServiceProvider> caller;
    @Inject
    private UserUiService userUiService;
    @Inject
    @DataField
    private Input nameField;
    @Inject
    @DataField
    private Div errorMessageDiv;
    @Inject
    @DataField
    private Button saveButton;
    private ModalDialogPanel<Void> modalDialogPanel;
    private boolean ok;

    @Override
    public void init(Void aVoid) {
        userUiService.clearSetUserNameTimer();
        nameField.addEventListener("change", event -> checkNameValid(), false);
        nameField.addEventListener("keyup", event -> checkNameValid(), false);
        checkNameValid();
    }

    @Override
    public void customize(ModalDialogPanel<Void> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }

    @EventHandler("saveButton")
    private void saveButtonClick(ClickEvent event) {
        caller.call((RemoteCallback<SetNameResult>) setNameResult -> {
            if (setNameResult.getUserName() != null) {
                userUiService.onUserNameSet(setNameResult.getUserName());
                ok = true;
                modalDialogPanel.close();
            } else {
                saveButton.setEnabled(false);
                displayError(setNameResult.getErrorResult());
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "UserServiceProvider.setName() failed: " + message, throwable);
            return false;
        }).setName(nameField.getValue());
    }

    private void checkNameValid() {
        saveButton.setEnabled(false);
        errorMessageDiv.setTextContent("");
        if (nameField.getValue() == null || nameField.getValue().trim().isEmpty()) {
            errorMessageDiv.setTextContent(I18nHelper.getConstants().nameToShort());
            return;
        }
        caller.call((RemoteCallback<SetNameResult>) errorResult -> {
            if (errorResult.getErrorResult() == null) {
                saveButton.setEnabled(true);
            } else {
                displayError(errorResult.getErrorResult());
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "UserServiceProvider.verifySetName() failed: " + message, throwable);
            return false;
        }).verifySetName(nameField.getValue());

    }

    private void displayError(ErrorResult errorResult) {
        errorMessageDiv.setTextContent("");
        switch (errorResult) {
            case TO_SHORT:
                errorMessageDiv.setTextContent(I18nHelper.getConstants().nameToShort());
                break;
            case ALREADY_USED:
                errorMessageDiv.setTextContent(I18nHelper.getConstants().nameAlreadyUsed());
                break;
            case UNKNOWN_ERROR:
                errorMessageDiv.setTextContent(I18nHelper.getConstants().unknownErrorReceived());
                break;
            default:
                logger.log(Level.SEVERE, "SetUserNameDialog.displayError() unknown SetNameResult.ErrorResult: " + errorResult);
        }
    }

    @Override
    public void onClose() {
        if (!ok) {
            userUiService.activateSetUserNameTimer();
        }
    }
}
