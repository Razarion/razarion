package com.btxtech.client.dialog;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 20.05.2016.
 */
@Templated("BootstrapModalDialog.html#modal-dialog")
public class BootstrapModalDialog extends Composite {
    @Inject
    private ModalDialog modalDialog;
    @Inject
    @DataField
    private Button closeCrossButton;
    @Inject
    @DataField
    private Button cancelButton;
    @Inject
    @DataField
    private Button applyButton;
    @Inject
    @DataField
    private Label headerLabel;
    @Inject
    @DataField
    private SimplePanel contentDiv;

    @EventHandler("closeCrossButton")
    private void closeCrossButtonClick(ClickEvent event) {
        modalDialog.cancel();
    }
    @EventHandler("cancelButton")
    private void cancelButtonClick(ClickEvent event) {
        modalDialog.cancel();
    }

    @EventHandler("applyButton")
    private void applyButtonButtonClick(ClickEvent event) {
        modalDialog.apply();
    }

    public void init(String title, ModalDialogContent content) {
        headerLabel.setText(title);
        contentDiv.setWidget(content);
    }

}
