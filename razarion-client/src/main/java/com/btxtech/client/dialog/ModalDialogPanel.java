package com.btxtech.client.dialog;

import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.utils.ControlUtils;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.uiservice.dialog.ApplyListener;
import com.google.gwt.dom.client.Style;
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
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 20.05.2016.
 */
@Templated("ModalDialogPanel.html#modal-dialog")
public class ModalDialogPanel<T> extends Composite {
    // private Logger logger = Logger.getLogger(ModalDialogPanel.class.getName());
    @Inject
    private Instance<ModalDialogContent> contentInstance;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
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
    @Inject
    @DataField
    private HTML buttonDiv;
    private ModalDialogContent<T> modalDialogContent;
    private T applyValue;
    private ApplyListener<T> applyListener;

    @PostConstruct
    public void postConstruct() {
        getElement().getStyle().setZIndex(ZIndexConstants.DIALOG);
    }

    public void init(String title, Class<? extends ModalDialogContent<T>> contentClass, T t, ApplyListener<T> applyListener) {
        this.applyListener = applyListener;
        modalDialogContent = contentInstance.select(contentClass).get();
        modalDialogContent.init(t);
        headerLabel.setText(title);
        content.setWidget(modalDialogContent);
        modalDialogContent.customize(this);
    }

    @EventHandler("closeCrossButton")
    private void closeCrossButtonClick(ClickEvent event) {
        modalDialogManager.close(this);
    }

    @EventHandler("cancelButton")
    private void cancelButtonClick(ClickEvent event) {
        modalDialogManager.close(this);
    }

    @EventHandler("applyButton")
    private void applyButtonButtonClick(ClickEvent event) {
        modalDialogManager.close(this);
        if (applyListener != null) {
            applyListener.onApply(applyValue);
        }
    }

    public void setApplyValue(T applyValue) {
        this.applyValue = applyValue;
    }

    public T getApplyValue() {
        return applyValue;
    }

    public void onClose() {
        modalDialogContent.onClose();
    }

    public void close() {
        modalDialogManager.close(this);
    }

    public void showApplyButton(boolean show) {
        applyButton.getElement().getStyle().setDisplay(show ? Style.Display.INLINE : Style.Display.NONE);
    }

    public void showCancelButton(boolean show) {
        cancelButton.getElement().getStyle().setDisplay(show ? Style.Display.INLINE : Style.Display.NONE);
    }

    public void addFooterButton(String text, Runnable callback) {
        buttonDiv.getElement().appendChild(GwtUtils.castElementToElement(ControlUtils.createButton(text, "btn btn-default", callback)));
    }
}
