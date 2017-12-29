package com.btxtech.client.dialog.common;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.dom.Button;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 30.10.2016.
 */
@Templated("UnnamedDialog.html#unnamedDialog")
public class UnnamedDialog extends Composite implements ModalDialogContent<String> {
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    @DataField
    private Label message;
    @Inject
    @DataField
    private Button setNameButton;
    private ModalDialogPanel<String> modalDialogPanel;

    @Override
    public void init(String message) {
        this.message.setText(message);
    }

    @Override
    public void onClose() {

    }

    @Override
    public void customize(ModalDialogPanel<String> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }

    @EventHandler("setNameButton")
    private void setNameButtonClick(ClickEvent event) {
        modalDialogPanel.close();
        modalDialogManager.showSetUserNameDialog();
    }

}
