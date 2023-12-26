package com.btxtech.client.dialog.framework;

import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.dialog.DialogButton;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
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
@Templated("ModalDialogPanel.html#glass-panel")
@Deprecated
public class ModalDialogPanel<T> implements IsElement {
    // private Logger logger = Logger.getLogger(ModalDialogPanel.class.getName());
    @Inject
    private Instance<ModalDialogContent> contentInstance;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    @DataField
    private HTMLDivElement glassPanelDiv;
    @Inject
    @DataField
    private Label headerLabel;
    @Inject
    @DataField
    private HTMLDivElement modalDialogDiv;
    @Inject
    @DataField
    private SimplePanel content;
    @Inject
    @DataField
    private Div buttonDiv;
    private ModalDialogContent<T> modalDialogContent;
    private T applyValue;
    private DialogButton.Listener<T> listener;
    private String title;

    @PostConstruct
    public void postConstruct() {
        glassPanelDiv.style.zIndex = ZIndexConstants.DIALOG;
        GwtUtils.preventContextMenu(glassPanelDiv);
    }

    @Override
    public HTMLElement getElement() {
        return glassPanelDiv;
    }

    public void init(String title, Class<? extends ModalDialogContent<T>> contentClass, T t, DialogButton.Listener<T> listener, DialogButton.Button... dialogButtons) {
        try {
            this.listener = listener;
            modalDialogContent = contentInstance.select(contentClass).get();
            modalDialogContent.init(t);
            headerLabel.setText(title);
            this.title = title;
            content.setWidget(modalDialogContent);
            modalDialogContent.customize(this);
            setupFooterButton(dialogButtons);
        } catch (Throwable throwable) {
            exceptionHandler.handleException("ModalDialogPanel.display() title: " + title, throwable);
        }
    }

    private void setupFooterButton(DialogButton.Button[] dialogButtons) {
        for (DialogButton.Button dialogButton : dialogButtons) {
            Button button = (Button) Window.getDocument().createElement("button");
            button.setTextContent(dialogButton.getText());
            button.addEventListener("click", event -> {
                        try {
                            modalDialogManager.close(ModalDialogPanel.this);
                            if (listener != null) {
                                listener.onPressed(dialogButton, applyValue);
                            }
                        } catch (Throwable t) {
                            exceptionHandler.handleException(t);
                        }
                    }
                    , false);
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

    public String getTitle() {
        return title;
    }

    public Rectangle getDialogRectangle() {
        int x = (int) (glassPanelDiv.offsetLeft + modalDialogDiv.offsetLeft);
        int y = (int) (glassPanelDiv.offsetTop + modalDialogDiv.offsetTop);
        return new Rectangle(x, y, (int) modalDialogDiv.clientWidth, (int) modalDialogDiv.clientHeight);
    }

    public void onShown() {
        modalDialogContent.onShown();
    }
}
