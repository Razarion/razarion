package com.btxtech.client.cockpit.chat;

import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.dialog.common.UnnamedDialog;
import com.btxtech.client.dialog.common.UnregisteredDialog;
import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.guielements.Div;
import com.btxtech.shared.datatypes.ChatMessage;
import com.btxtech.uiservice.cockpit.ChatCockpit;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.dialog.DialogButton;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.user.UserUiService;
import com.google.gwt.event.dom.client.ClickEvent;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.Button;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Event;
import org.jboss.errai.common.client.dom.EventListener;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Input;
import org.jboss.errai.common.client.dom.KeyboardEvent;
import org.jboss.errai.common.client.dom.MouseEvent;
import org.jboss.errai.common.client.dom.Window;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 27.12.2017.
 */
@Templated("ClientChatCockpit.html#chatCockpit")
public class ClientChatCockpit implements ChatCockpit, IsElement {
    private static final int RESIZE_CURSOR_AREA = 10;
    // private Logger logger = Logger.getLogger(ClientChatCockpit.class.getName());
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    private UserUiService userUiService;
    @Inject
    @DataField
    private Div chatCockpit;
    @Inject
    @DataField
    private Div messageDiv;
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<ChatMessage, MessageWidget> messageTable;
    @Inject
    @DataField
    private Button sendButton;
    @Inject
    @DataField
    private Input messageInput;
    private boolean resizeMode;

    @Override
    public void show() {
        DOMUtil.removeAllElementChildren(messageTable.getElement()); // Remove placeholder table row from template.
        Window.getDocument().getBody().appendChild(chatCockpit);
        chatCockpit.getStyle().setProperty("z-index", Integer.toString(ZIndexConstants.CHAT_COCKPIT));
        chatCockpit.addEventListener("mousemove", (EventListener<MouseEvent>) event -> {
            if (isResizeAllowed(event)) {
                getElement().getStyle().setProperty("cursor", "se-resize");
            } else {
                getElement().getStyle().setProperty("cursor", "default");
            }
        }, false);
        EventListener<MouseEvent> mouseMoveListener = event -> {
            if (resizeMode) {
                doResize(event);
            }
        };
        chatCockpit.addEventListener("mousedown", (EventListener<MouseEvent>) event -> {
            if (!resizeMode && isResizeAllowed(event)) {
                resizeMode = true;
                doResize(event);
                Window.getDocument().getBody().addEventListener("mousemove", mouseMoveListener, true);
            }
        }, false);
        chatCockpit.addEventListener("mouseup", (EventListener<MouseEvent>) event -> {
            if (resizeMode) {
                Window.getDocument().getBody().removeEventListener("mousemove", mouseMoveListener, true);
                resizeMode = false;
            }
        }, false);
        messageInput.addEventListener("keydown", (EventListener<KeyboardEvent>) event -> {
            if(event.getKeyCode() == 13) {
                send();
            }
        }, false);
    }

    private boolean isResizeAllowed(MouseEvent event) {
        return RESIZE_CURSOR_AREA > event.getOffsetX() || RESIZE_CURSOR_AREA > event.getOffsetY();
    }

    private void doResize(MouseEvent event) {
        chatCockpit.getStyle().setProperty("width", Integer.toString(chatCockpit.getClientWidth() - event.getMovementX()) + "px");
        chatCockpit.getStyle().setProperty("height", Integer.toString(chatCockpit.getClientHeight() - event.getMovementY()) + "px");
    }

    @Override
    public void displayMessages(List<ChatMessage> messages) {
        messageTable.setValue(Collections.emptyList());
        messageTable.setValue(messages);
        messageDiv.setScrollTop(Integer.MAX_VALUE);
    }

    @Override
    public HTMLElement getElement() {
        return chatCockpit;
    }

    @EventHandler("sendButton")
    private void onSendButtonClicked(ClickEvent event) {
        send();
    }

    private void send() {
        if (!userUiService.isRegistered()) {
            modalDialogManager.show(I18nHelper.getConstants().unregistered(), ClientModalDialogManagerImpl.Type.QUEUE_ABLE, UnregisteredDialog.class, I18nHelper.getConstants().chatUnregistered(), null, null, DialogButton.Button.CANCEL);
            return;
        } else if (!userUiService.isRegisteredAndNamed()) {
            modalDialogManager.show(I18nHelper.getConstants().unnamed(), ClientModalDialogManagerImpl.Type.QUEUE_ABLE, UnnamedDialog.class, I18nHelper.getConstants().chatUnnamed(), null, null, DialogButton.Button.CANCEL);
            return;
        }

        if (messageInput.getValue() == null || messageInput.getValue().trim().length() == 0) {
            return;
        }
        gameUiControl.sendChatMessage(messageInput.getValue());
        messageInput.setValue("");
    }
}
