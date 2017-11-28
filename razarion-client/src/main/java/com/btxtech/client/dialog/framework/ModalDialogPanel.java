package com.btxtech.client.dialog.framework;

import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.dialog.DialogButton;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import org.jboss.errai.common.client.dom.Button;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Window;
import org.jboss.errai.ui.shared.api.annotations.DataField;
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
    private ExceptionHandler exceptionHandler;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    @DataField
    private Label headerLabel;
    @Inject
    @DataField
    private SimplePanel content;
    @Inject
    @DataField
    private Div buttonDiv;
    private ModalDialogContent<T> modalDialogContent;
    private T applyValue;
    private DialogButton.Listener<T> listener;

    @PostConstruct
    public void postConstruct() {
        getElement().getStyle().setZIndex(ZIndexConstants.DIALOG);
        GwtUtils.preventContextMenu(this);
    }

    public void init(String title, Class<? extends ModalDialogContent<T>> contentClass, T t, DialogButton.Listener<T> listener, DialogButton.Button... dialogButtons) {
        try {
            this.listener = listener;
            modalDialogContent = contentInstance.select(contentClass).get();
            modalDialogContent.init(t);
            headerLabel.setText(title);
            content.setWidget(modalDialogContent);
            modalDialogContent.customize(this);
            setupFooterButton(dialogButtons);
        } catch (Throwable throwable) {
            exceptionHandler.handleException("ModalDialogPanel.init() title: " + title, throwable);
        }
    }

    private void setupFooterButton(DialogButton.Button[] dialogButtons) {
        for (DialogButton.Button dialogButton : dialogButtons) {
            Button button = (Button) Window.getDocument().createElement("button");
            button.setTextContent(dialogButton.getText());
            button.addEventListener("click", event -> {
                modalDialogManager.close(ModalDialogPanel.this);
                if (listener != null) {
                    listener.onPressed(dialogButton, applyValue);
                }
            }, false);
            buttonDiv.appendChild(button);
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

    public void addNonClosableFooterButton(String text, Runnable callback) {
        Button button = (Button) Window.getDocument().createElement("button");
        button.setTextContent(text);
        button.addEventListener("click", event -> {
            if (callback != null) {
                callback.run();
            }
        }, false);
        buttonDiv.appendChild(button);
    }
}
