package com.btxtech.client.editor.widgets.bot.selector;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.common.DisplayUtils;
import com.btxtech.shared.rest.CommonEditorProvider;
import com.btxtech.uiservice.dialog.DialogButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 22.08.2017.
 */
@Templated("BotListWidget.html#botRow")
public class BotEntryWidget implements TakesValue<BotEntryModel>, IsElement {
    private Logger logger = Logger.getLogger(BotEntryWidget.class.getName());
    @Inject
    private Caller<CommonEditorProvider> provider;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    @DataField
    private TableRow botRow;
    @Inject
    @DataField
    private Span botIdSpan;
    @Inject
    @DataField
    private Span botInternalNameSpan;
    @Inject
    @DataField
    private Button botChangeButton;
    @Inject
    @DataField
    private Button botDeleteButton;
    private BotEntryModel botEntryModel;


    @Override
    public void setValue(BotEntryModel botEntryModel) {
        this.botEntryModel = botEntryModel;
        setupFields();
    }

    @Override
    public BotEntryModel getValue() {
        return botEntryModel;
    }

    @Override
    public HTMLElement getElement() {
        return botRow;
    }

    @EventHandler("botChangeButton")
    private void botChangeButtonClicked(ClickEvent event) {
        modalDialogManager.show("Bots", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, BotSelectionDialog.class, botEntryModel.getBotId(), (button, selectedBotId) -> {
            if (button == DialogButton.Button.APPLY) {
                botEntryModel.change(selectedBotId);
                setupFields();
            }
        }, null, DialogButton.Button.CANCEL, DialogButton.Button.APPLY);
    }

    @EventHandler("botDeleteButton")
    private void botDeleteButtonClicked(ClickEvent event) {
        botEntryModel.remove();
    }

    private void setupFields() {
        botIdSpan.setTextContent(DisplayUtils.handleInteger(botEntryModel.getBotId()));
        if(botEntryModel.getBotId() != null) {
            botInternalNameSpan.setTextContent("<Loading>");
            provider.call(response -> botInternalNameSpan.setTextContent((String)response), (message, throwable) -> {
                logger.log(Level.SEVERE, "CommonEditorProvider.getInternalNameBot() failed: " + message, throwable);
                return false;
            }).getInternalNameBot(botEntryModel.getBotId());
        } else {
            botInternalNameSpan.setTextContent("");
        }
    }
}
