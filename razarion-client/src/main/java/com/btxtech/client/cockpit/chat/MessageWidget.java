package com.btxtech.client.cockpit.chat;

import com.btxtech.shared.datatypes.ChatMessage;
import com.btxtech.uiservice.Colors;
import com.btxtech.uiservice.user.UserUiService;
import com.google.gwt.user.client.TakesValue;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 29.12.2017.
 */
@Templated("ClientChatCockpit.html#messageRow")
public class MessageWidget implements TakesValue<ChatMessage>, IsElement {
    @Inject
    private UserUiService userUiService;
    @Inject
    @DataField
    private TableRow messageRow;
    @Inject
    @DataField
    private Span userEntrySpan;
    @Inject
    @DataField
    private Span messageEntrySpan;
    private ChatMessage chatMessage;

    @Override
    public void setValue(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
        if(userUiService.isRegistered() && userUiService.getUserContext().getHumanPlayerId().getUserId() == chatMessage.getUserId()) {
            userEntrySpan.getStyle().setProperty("color", Colors.OWN.toHtmlColor());
        } else {
            userEntrySpan.getStyle().setProperty("color", Colors.FRIEND.toHtmlColor());
        }
        userEntrySpan.setTextContent(chatMessage.getUserName() + ": ");
        messageEntrySpan.setTextContent(chatMessage.getMessage());
    }

    @Override
    public ChatMessage getValue() {
        return chatMessage;
    }

    @Override
    public HTMLElement getElement() {
        return messageRow;
    }
}
