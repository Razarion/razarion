package com.btxtech.client.editor.widgets.itemtype.base;

import com.btxtech.common.DisplayUtils;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 22.08.2016.
 */

@Templated("BaseItemTypeSelectionDialog.html#tableRow")
public class BaseItemTypeSelectionEntry implements TakesValue<BaseItemType>, IsElement {
    @Inject
    @DataField
    private TableRow tableRow;
    @Inject
    @DataField
    private Label baseItemTypeId;
    @Inject
    @DataField
    private Label baseItemTypeName;
    private BaseItemType baseItemType;
    private BaseItemTypeSelectionDialog baseItemTypeSelectionDialog;

    @Override
    public HTMLElement getElement() {
        return tableRow;
    }

    @Override
    public void setValue(BaseItemType baseItemType) {
        this.baseItemType = baseItemType;
        baseItemTypeId.setText(DisplayUtils.handleInteger(baseItemType.getId()));
        baseItemTypeName.setText(baseItemType.getInternalName());
    }

    @Override
    public BaseItemType getValue() {
        return baseItemType;
    }

    @EventHandler("tableRow")
    public void onClick(final ClickEvent event) {
        baseItemTypeSelectionDialog.selectComponent(this);
    }

    public void setSelected(boolean selected) {
        if (selected) {
            DOMUtil.addCSSClass(tableRow, "generic-gallery-table-row-selected");
            DOMUtil.removeCSSClass(tableRow, "generic-gallery-table-row-not-selected");
        } else {
            DOMUtil.addCSSClass(tableRow, "generic-gallery-table-row-not-selected");
            DOMUtil.removeCSSClass(tableRow, "generic-gallery-table-row-selected");
        }
    }

    public void setBaseItemTypeSelectionDialog(BaseItemTypeSelectionDialog baseItemTypeSelectionDialog) {
        this.baseItemTypeSelectionDialog = baseItemTypeSelectionDialog;
    }
}
