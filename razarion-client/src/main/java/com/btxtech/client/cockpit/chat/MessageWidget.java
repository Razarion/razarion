package com.btxtech.client.cockpit.chat;

import com.btxtech.shared.datatypes.ChatMessage;
import com.btxtech.uiservice.Colors;
import com.btxtech.uiservice.user.UserUiService;
import com.google.gwt.user.client.TakesValue;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Beat
 * on 29.12.2017.
 */
@Templated("ClientChatCockpit.html#messageDiv")
public class MessageWidget implements TakesValue<ChatMessage>, IsElement {
    @Inject
    private UserUiService userUiService;
    @Inject
    @DataField
    private HTMLDivElement messageDiv;
    @Inject
    @DataField
    @Named("Span")
    private HTMLElement userEntrySpan;
    @Inject
    @DataField
    @Named("Span")
    private HTMLElement messageEntrySpan;
    private ChatMessage chatMessage;

    @Override
    public void setValue(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
        if (userUiService.isRegistered() && userUiService.getUserContext().getHumanPlayerId().getUserId() == chatMessage.getUserId()) {
            userEntrySpan.style.setProperty("color", Colors.OWN.toHtmlColor());
        } else {
            userEntrySpan.style.setProperty("color", Colors.FRIEND.toHtmlColor());
        }
        userEntrySpan.textContent = chatMessage.getUserName() + ": ";
        messageEntrySpan.textContent = chatMessage.getMessage();
    }

    @Override
    public ChatMessage getValue() {
        return chatMessage;
    }

    @Override
    public org.jboss.errai.common.client.dom.HTMLElement getElement() {
        return (org.jboss.errai.common.client.dom.HTMLElement) messageDiv;
    }
}
