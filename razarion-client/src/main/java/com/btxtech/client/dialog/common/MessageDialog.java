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
@Templated("MessageDialog.html#message")
public class MessageDialog extends Composite implements ModalDialogContent<String> {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label message;

    @Override
    public void init(String message) {
        this.message.setText(message);
    }

    @Override
    public void onClose() {

    }

    @Override
    public void customize(ModalDialogPanel<String> modalDialogPanel) {

    }
}
