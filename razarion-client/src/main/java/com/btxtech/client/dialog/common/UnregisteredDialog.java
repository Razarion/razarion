package com.btxtech.client.dialog.common;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 30.10.2016.
 */
@Templated("UnregisteredDialog.html#unregisteredDialog")
public class UnregisteredDialog extends Composite implements ModalDialogContent<String> {
    @Inject
    @DataField
    private Label message;
    private ModalDialogPanel<String> modalDialogPanel;

    // TODO show register dialog button
    // TODO also in ClientSideCockpit

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
}
