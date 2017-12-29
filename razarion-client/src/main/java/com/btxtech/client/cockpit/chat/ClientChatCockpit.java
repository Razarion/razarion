package com.btxtech.client.cockpit.chat;

import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.guielements.Div;
import com.btxtech.uiservice.cockpit.ChatCockpit;
import com.btxtech.uiservice.control.GameUiControl;
import com.google.gwt.event.dom.client.ClickEvent;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.Button;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.EventListener;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Input;
import org.jboss.errai.common.client.dom.MouseEvent;
import org.jboss.errai.common.client.dom.Window;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.Arrays;

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
    @DataField
    private Div chatCockpit;
    @Inject
    @DataField
    private Div messageDiv;
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<MessageModel, MessageWidget> messageTable;
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
        // TODO --- remove
        messageTable.setValue(Arrays.asList(new MessageModel().setUserName("beat").setMessage("adsfol dasfdasofui rawgh'üokjarew adsfjunouigfh asrfghdrfashouiuha  asg arsw graew gh"), new MessageModel().setUserName("beat").setMessage("adsfol dasfdasofui rawgh'üokjarew adsfjunouigfh asrfghdrfashouiuha  asg arsw graew gh"), new MessageModel().setUserName("beat").setMessage("adsfol dasfdasofui rawgh'üokjarew adsfjunouigfh asrfghdrfashouiuha  asg arsw graew gh"), new MessageModel().setUserName("beat").setMessage("adsfol dasfdasofui rawgh'üokjarew adsfjunouigfh asrfghdrfashouiuha  asg arsw graew gh")));
        // TODO --- remove ends
        messageDiv.setScrollTop(Integer.MAX_VALUE);
    }

    private boolean isResizeAllowed(MouseEvent event) {
        return RESIZE_CURSOR_AREA > event.getOffsetX() || RESIZE_CURSOR_AREA > event.getOffsetY();
    }

    private void doResize(MouseEvent event) {
        chatCockpit.getStyle().setProperty("width", Integer.toString(chatCockpit.getClientWidth() - event.getMovementX()) + "px");
        chatCockpit.getStyle().setProperty("height", Integer.toString(chatCockpit.getClientHeight() - event.getMovementY()) + "px");
    }

    @Override
    public HTMLElement getElement() {
        return chatCockpit;
    }

    @EventHandler("sendButton")
    private void onSendButtonClicked(ClickEvent event) {
        if (messageInput.getValue() == null || messageInput.getValue().trim().length() == 0) {
            return;
        }
        gameUiControl.sendChatMessage(messageInput.getValue());
        messageInput.setValue("");
    }
}
