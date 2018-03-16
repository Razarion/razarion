package com.btxtech.client.cockpit.chat;

import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.dialog.common.UnnamedDialog;
import com.btxtech.client.dialog.common.UnregisteredDialog;
import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.shared.datatypes.ChatMessage;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.uiservice.cockpit.ChatCockpit;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.dialog.DialogButton;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.user.UserUiService;
import com.google.gwt.event.dom.client.ClickEvent;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.MouseEvent;
import org.jboss.errai.common.client.dom.DOMUtil;
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
public class ClientChatCockpit implements ChatCockpit {
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
    private HTMLDivElement chatCockpit;
    @Inject
    @DataField
    @ListContainer("div")
    private ListComponent<ChatMessage, MessageWidget> messagesDiv;
    @Inject
    @DataField
    private HTMLButtonElement sendButton;
    @Inject
    @DataField
    private HTMLInputElement messageInput;
    private boolean resizeMode;
    private Index startResizePosition;

    @Override
    public void show() {
        DOMUtil.removeAllElementChildren(messagesDiv.getElement()); // Remove placeholder table row from template.
        DomGlobal.document.body.appendChild(chatCockpit);
        chatCockpit.style.setProperty("z-index", Integer.toString(ZIndexConstants.CHAT_COCKPIT));
        chatCockpit.addEventListener("mousemove", event -> {
            if (isResizeAllowed((MouseEvent) event)) {
                chatCockpit.style.setProperty("cursor", "se-resize");
            } else {
                chatCockpit.style.setProperty("cursor", "default");
            }
        }, false);
        EventListener mouseMoveListener = event -> {
            if (resizeMode) {
                doResize((MouseEvent) event);
            }
        };
        chatCockpit.addEventListener("mousedown", event -> {
            if (!resizeMode && isResizeAllowed((MouseEvent) event)) {
                resizeMode = true;
                startResizePosition = new Index((int) ((MouseEvent) event).clientX, (int) ((MouseEvent) event).clientY);
                doResize((MouseEvent) event);
                DomGlobal.document.body.addEventListener("mousemove", mouseMoveListener, true);
            }
        }, false);
        chatCockpit.addEventListener("mouseup", event -> {
            if (resizeMode) {
                DomGlobal.document.body.removeEventListener("mousemove", mouseMoveListener, true);
                resizeMode = false;
                startResizePosition = null;
            }
        }, false);
        messageInput.addEventListener("keydown", (Event event) -> {
            if (((KeyboardEvent) event).key.equalsIgnoreCase("Enter")) {
                send();
            }
        }, false);
    }

    private boolean isResizeAllowed(MouseEvent event) {
        return RESIZE_CURSOR_AREA > (event.clientX - chatCockpit.offsetLeft) || RESIZE_CURSOR_AREA > (event.clientY - chatCockpit.offsetTop);
    }

    private void doResize(MouseEvent event) {
        Index current = new Index((int) event.clientX, (int) event.clientY);
        Index delta = current.sub(startResizePosition);
        startResizePosition = current;
        chatCockpit.style.setProperty("width", Integer.toString(chatCockpit.clientWidth - delta.getX()) + "px");
        chatCockpit.style.setProperty("height", Integer.toString(chatCockpit.clientHeight - delta.getY()) + "px");
    }

    @Override
    public void displayMessages(List<ChatMessage> messages) {
        messagesDiv.setValue(Collections.emptyList());
        messagesDiv.setValue(messages);
        if(messagesDiv.getValue().size() > 0) {
            ((HTMLElement)messagesDiv.getComponent(messagesDiv.getValue().size() - 1).getElement()).scrollIntoView();
        }
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

        if (messageInput.value == null || messageInput.value.trim().length() == 0) {
            return;
        }
        gameUiControl.sendChatMessage(messageInput.value);
        messageInput.value = "";
    }
}
