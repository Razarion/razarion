package com.btxtech.client.dialog;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 20.05.2016.
 */
@Templated("BootstrapModalDialog.html#modal-dialog")
public class BootstrapModalDialog extends Composite {
    private Logger logger = Logger.getLogger(BootstrapModalDialog.class.getName());
    @Inject
    private ModalDialogManager modalDialogManager;
    @Inject
    @DataField
    private Button closeCrossButton;
    @Inject
    @DataField
    private HTML buttonDiv;
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
        modalDialogManager.cancel();
    }

    @EventHandler("cancelButton")
    private void cancelButtonClick(ClickEvent event) {
        modalDialogManager.cancel();
    }

    @EventHandler("applyButton")
    private void applyButtonButtonClick(ClickEvent event) {
        modalDialogManager.apply();
    }

    public void init(String title, ModalDialogContent content) {
        headerLabel.setText(title);
        contentDiv.setWidget(content);
    }

    public void appendWidgetToButtonPanel(Element element) {
        element.setClassName("btn btn-default");
        buttonDiv.getElement().appendChild(element);
    }
}
