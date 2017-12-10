package com.btxtech.client.editor.widgets.bot.selector;

import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.shared.dto.ObjectNameId;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 22.08.2017.
 */
@Templated("BotSelectionDialog.html#botRow")
public class BotSelectionEntryWidget implements TakesValue<ObjectNameId>, IsElement {
    @Inject
    @DataField
    private TableRow botRow;
    @Inject
    @DataField
    private Span botIdSpan;
    @Inject
    @DataField
    private Span botInternalNameSpan;
    private ObjectNameId botEntryModel;
    private BotSelectionDialog botSelectionDialog;

    @Override
    public void setValue(ObjectNameId objectNameId) {
        this.botEntryModel = objectNameId;
        botIdSpan.setTextContent(DisplayUtils.handleInteger(objectNameId.getId()));
        botInternalNameSpan.setTextContent(objectNameId.getInternalName());
    }

    @Override
    public ObjectNameId getValue() {
        return botEntryModel;
    }

    @Override
    public HTMLElement getElement() {
        return botRow;
    }

    @EventHandler("botRow")
    public void onClick(final ClickEvent event) {
        botSelectionDialog.selectComponent(this);
    }

    public void setBotSelectionDialog(BotSelectionDialog botSelectionDialog) {
        this.botSelectionDialog = botSelectionDialog;
    }

    public void setSelected(boolean selected) {
        if (selected) {
            DOMUtil.addCSSClass(botRow, "generic-gallery-table-row-selected");
            DOMUtil.removeCSSClass(botRow, "generic-gallery-table-row-not-selected");
        } else {
            DOMUtil.addCSSClass(botRow, "generic-gallery-table-row-not-selected");
            DOMUtil.removeCSSClass(botRow, "generic-gallery-table-row-selected");
        }
    }
}
