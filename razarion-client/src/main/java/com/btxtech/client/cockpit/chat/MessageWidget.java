package com.btxtech.client.cockpit.chat;

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
public class MessageWidget implements TakesValue<MessageModel>, IsElement {
    @Inject
    @DataField
    private TableRow messageRow;
    @Inject
    @DataField
    private Span userEntrySpan;
    @Inject
    @DataField
    private Span messageEntrySpan;
    private MessageModel messageModel;

    @Override
    public void setValue(MessageModel messageModel) {
        this.messageModel = messageModel;
        userEntrySpan.setTextContent(messageModel.getUserName());
        messageEntrySpan.setTextContent(messageModel.getMessage());
    }

    @Override
    public MessageModel getValue() {
        return messageModel;
    }

    @Override
    public HTMLElement getElement() {
        return messageRow;
    }
}
