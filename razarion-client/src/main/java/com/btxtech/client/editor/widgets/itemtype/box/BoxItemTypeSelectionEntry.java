package com.btxtech.client.editor.widgets.itemtype.box;

import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
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

@Templated("BoxItemTypeSelectionDialog.html#tableRow")
public class BoxItemTypeSelectionEntry implements TakesValue<BoxItemType>, IsElement {
    @Inject
    @DataField
    private TableRow tableRow;
    @Inject
    @DataField
    private Label boxItemTypeId;
    @Inject
    @DataField
    private Label boxItemTypeName;
    private BoxItemType boxItemType;
    private BoxItemTypeSelectionDialog boxItemTypeSelectionDialog;

    @Override
    public HTMLElement getElement() {
        return tableRow;
    }

    @Override
    public void setValue(BoxItemType boxItemType) {
        this.boxItemType = boxItemType;
        boxItemTypeId.setText(DisplayUtils.handleInteger(boxItemType.getId()));
        boxItemTypeName.setText(boxItemType.getInternalName());
    }

    @Override
    public BoxItemType getValue() {
        return boxItemType;
    }

    @EventHandler("tableRow")
    public void onClick(final ClickEvent event) {
        boxItemTypeSelectionDialog.selectComponent(this);
    }

    public void setBoxItemTypeSelectionDialog(BoxItemTypeSelectionDialog boxItemTypeSelectionDialog) {
        this.boxItemTypeSelectionDialog = boxItemTypeSelectionDialog;
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
}
