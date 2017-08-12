package com.btxtech.client.editor.widgets.itemtype.resource;

import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
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

import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Created by Beat
 * 22.08.2016.
 */

@Templated("ResourceItemTypeSelectionDialog.html#tableRow")
public class ResourceItemTypeSelectionEntry implements TakesValue<ResourceItemType>, IsElement {
    @Inject
    private Event<ResourceItemTypeSelectionEntry> eventTrigger;
    @Inject
    @DataField
    private TableRow tableRow;
    @Inject
    @DataField
    private Label resourceItemTypeId;
    @Inject
    @DataField
    private Label resourceItemTypeName;
    private ResourceItemType resourceItemType;

    @Override
    public HTMLElement getElement() {
        return tableRow;
    }

    @Override
    public void setValue(ResourceItemType resourceItemType) {
        this.resourceItemType = resourceItemType;
        resourceItemTypeId.setText(DisplayUtils.handleInteger(resourceItemType.getId()));
        resourceItemTypeName.setText(resourceItemType.getInternalName());
    }

    @Override
    public ResourceItemType getValue() {
        return resourceItemType;
    }

    @EventHandler("tableRow")
    public void onClick(final ClickEvent event) {
        eventTrigger.fire(this);
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
