package com.btxtech.client.dialog;

import com.btxtech.client.cockpit.ZIndexConstants;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 20.05.2016.
 */
@Templated("ModalDialogPanel.html#modal-dialog")
public class ModalDialogPanel extends Composite {
    // private Logger logger = Logger.getLogger(ModalDialogPanel.class.getName());
    @Inject
    private ModalDialogManager modalDialogManager;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button closeCrossButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button cancelButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button applyButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label headerLabel;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private SimplePanel content;

    @PostConstruct
    public void init() {
        getElement().getStyle().setZIndex(ZIndexConstants.DIALOG);
    }

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
        this.content.setWidget(content);
    }
}
