package com.btxtech.client.dialog.common;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.UserAccountInfo;
import com.btxtech.shared.rest.UserServiceProvider;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.user.UserUiService;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.dom.CheckboxInput;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.common.client.dom.TableRow;
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
@Templated("UserAccountDialog.html#userAccountDialog")
public class UserAccountDialog extends Composite implements ModalDialogContent<Void> {
    private Logger logger = Logger.getLogger(UserAccountDialog.class.getName());
    @Inject
    private Caller<UserServiceProvider> caller;
    @Inject
    private UserUiService userUiService;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    @DataField
    private Span loggedInSpan;
    @Inject
    @DataField
    private Button setNameButton;
    @Inject
    @DataField
    private Span userNameSpan;
    @Inject
    @DataField
    private TableRow rememberMeTr;
    @Inject
    @DataField
    private CheckboxInput rememberMeCheckbox;
    @Inject
    @DataField
    private Button logoutButton;
    private ModalDialogPanel<Void> modalDialogPanel;

    @Override
    public void init(Void aVoid) {
        if (userUiService.isRegisteredAndNamed()) {
            setNameButton.getElement().getStyle().setDisplay(Style.Display.NONE);
            userNameSpan.getStyle().setProperty("display", "inline");
            userNameSpan.setTextContent(userUiService.getUserContext().getName());
        } else {
            setNameButton.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
            userNameSpan.getStyle().setProperty("display", "none");
        }
        caller.call((RemoteCallback<UserAccountInfo>) userAccountInfo -> {
            if (userAccountInfo.getEmail() != null) {
                loggedInSpan.setTextContent(userAccountInfo.getEmail());
                rememberMeTr.getStyle().setProperty("display", "table-row");
                rememberMeCheckbox.setChecked(userAccountInfo.isRememberMe());
            } else {
                loggedInSpan.setTextContent("Facebook");
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "UserAccountDialog: userAccountInfo() failed: " + message, throwable);
            return false;
        }).userAccountInfo();
    }

    @Override
    public void customize(ModalDialogPanel<Void> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }

    @EventHandler("rememberMeCheckbox")
    public void rememberMeCheckboxClicked(ChangeEvent event) {
        caller.call(ignore -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "UserAccountDialog: setRememberMe() failed: " + message, throwable);
            return false;
        }).setRememberMe(rememberMeCheckbox.getChecked());
    }

    @EventHandler("setNameButton")
    private void setNameButtonClick(ClickEvent event) {
        modalDialogPanel.close();
        modalDialogManager.showSetUserNameDialog();
    }

    @EventHandler("logoutButton")
    private void logoutButtonClick(ClickEvent event) {
        modalDialogManager.showQuestionDialog(I18nHelper.getConstants().logout(), I18nHelper.getConstants().logoutQuestion(), () -> Window.Location.replace(CommonUrl.LOGOUT_PAGE), null);
    }

    @Override
    public void onClose() {
    }
}
